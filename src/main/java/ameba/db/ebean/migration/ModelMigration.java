package ameba.db.ebean.migration;

import ameba.db.migration.models.MigrationInfo;
import ameba.exception.AmebaException;
import com.avaje.ebean.config.dbplatform.DbPlatformName;
import com.avaje.ebean.dbmigration.DbMigration;
import com.avaje.ebean.dbmigration.DbOffline;
import com.avaje.ebean.dbmigration.DdlRunner;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebean.dbmigration.model.MConfiguration;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebean.dbmigration.model.ModelDiff;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author icode
 */
public class ModelMigration extends DbMigration {
    private static final String initialVersion = "1.0";
    private ModelDiff diff;
    private MigrationModel migrationModel;
    private CurrentModel currentModel;
    private MigrationInfo migrationInfo;

    public ModelDiff diff() {
        if (diff != null) return diff;
        // use this flag to stop other plugins like full DDL generation
        if (!online) {
            DbOffline.setRunningMigration();
        }

        setDefaults();

        try {
            migrationModel = new MigrationModel(server);
            ModelContainer migrated = migrationModel.read();

            currentModel = new CurrentModel(server, constraintNaming);
            ModelContainer current = currentModel.read();

            diff = new ModelDiff(migrated);
            diff.compareTo(current);

            if (!migrationModel.isMigrationTableExist()) {
                DdlRunner runner = new DdlRunner(false, "create_migration_info_table.ddl");
                DdlHandler handler = server.getDatabasePlatform().createDdlHandler(server.getServerConfig());
                BeanDescriptor<MigrationInfo> beanDescriptor = server.getBeanDescriptor(MigrationInfo.class);
                DdlWrite write = new DdlWrite(new MConfiguration(), currentModel.read());
                handler.generate(write, current.getTable(beanDescriptor.getBaseTable()).createTable());
                String ddl = write.apply().getBuffer() +
                        write.applyForeignKeys().getBuffer() +
                        write.applyHistory().getBuffer();
                runner.runAll(ddl, server);
            }
        } catch (IOException e) {
            throw new AmebaException(e);
        } finally {
            if (!online) {
                DbOffline.reset();
            }
        }
        return diff;
    }

    @Override
    public void generateMigration() throws IOException {
        // use this flag to stop other plugins like full DDL generation
        if (!online) {
            DbOffline.setRunningMigration();
        }
        try {
            if (migrationInfo != null) {
                return;
            }
            if (diff == null) {
                diff();
            }

            if (diff.isEmpty()) {
                logger.info("no changes detected - no migration written");
                return;
            }
            // there were actually changes to write
            Migration dbMigration = diff.getMigration();

            String version = getVersion(migrationModel);
            logger.info("generating migration:{}", version);
            if (!writeMigrationXml(dbMigration, version)) {
                logger.warn("migration already exists, not generating DDL");
            } else {
                if (databasePlatform != null) {
                    // writer needs the current model to provide table/column details for
                    // history ddl generation (triggers, history tables etc)
                    DdlWrite write = new DdlWrite(new MConfiguration(), currentModel.read());
                    PlatformDdlWriter writer = createDdlWriter();
                    writer.processMigration(dbMigration, write);
                }
            }

        } finally {
            if (!online) {
                DbOffline.reset();
            }
        }
    }

    @Override
    public void addPlatform(DbPlatformName platform, String prefix) {
        throw new UnsupportedOperationException("addPlatform Unsupported");
    }

    @Override
    protected void writeExtraPlatformDdl(String fullVersion, CurrentModel currentModel,
                                         Migration dbMigration, File writePath) throws IOException {
        throw new UnsupportedOperationException("writeExtraPlatformDdl Unsupported");
    }

    protected boolean writeMigrationXml(Migration dbMigration, File resourcePath, String fullVersion) {
        throw new UnsupportedOperationException("writeExtraPlatformDdl Unsupported");
    }

    protected boolean writeMigrationXml(Migration dbMigration, String version) {
        if (migrationModel.isMigrationTableExist()) {
            migrationInfo = server.find(MigrationInfo.class, version);
            if (migrationInfo != null) {
                return false;
            }
        }
        migrationInfo = new MigrationInfo();
        migrationInfo.setRevision(version);
        try (StringWriter writer = new StringWriter()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(Migration.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(dbMigration, writer);
            writer.flush();
            migrationInfo.setModelDiff(writer.toString());
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    protected PlatformDdlWriter createDdlWriter() {
        return new PlatformDdlWriter(migrationInfo, migrationConfig, (SpiServer) server);
    }

    /**
     * Return the full version for the migration being generated.
     */
    private String getVersion(MigrationModel migrationModel) {

        String version = migrationConfig.getVersion();
        if (version == null) {
            version = migrationModel.getNextVersion(initialVersion);
        }

        return version;
    }

    public ModelDiff rest() {
        ModelDiff old = diff;
        diff = null;
        migrationModel = null;
        currentModel = null;
        migrationInfo = null;
        return old;
    }

    public MigrationInfo getMigrationInfo() {
        return migrationInfo;
    }
}
