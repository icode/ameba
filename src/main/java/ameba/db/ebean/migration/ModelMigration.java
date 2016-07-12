package ameba.db.ebean.migration;

import ameba.db.migration.models.ScriptInfo;
import ameba.exception.AmebaException;
import com.avaje.ebean.config.dbplatform.DbPlatformName;
import com.avaje.ebean.dbmigration.DbMigration;
import com.avaje.ebean.dbmigration.DbOffline;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebean.dbmigration.model.MConfiguration;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebean.dbmigration.model.ModelDiff;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.util.JdbcClose;
import org.avaje.dbmigration.ddl.DdlRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author icode
 */
public class ModelMigration extends DbMigration {
    private static final String initialVersion = "1.0";
    private ModelDiff diff;
    private MigrationModel migrationModel;
    private CurrentModel currentModel;
    private ScriptInfo scriptInfo;

    public ModelDiff diff() {
        if (diff != null) return diff;

        setOffline();

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
                BeanDescriptor<ScriptInfo> beanDescriptor = server.getBeanDescriptor(ScriptInfo.class);
                DdlWrite write = new DdlWrite(new MConfiguration(), currentModel.read());
                handler.generate(write, current.getTable(beanDescriptor.getBaseTable()).createTable());
                String ddl = write.apply().getBuffer() +
                        write.applyForeignKeys().getBuffer() +
                        write.applyHistory().getBuffer();
                Connection connection = server.createTransaction().getConnection();
                try {
                    runner.runAll(ddl, connection);
                } finally {
                    JdbcClose.close(connection);
                }
            }
        } catch (IOException | SQLException e) {
            throw new AmebaException(e);
        } finally {
            if (!online) {
                DbOffline.reset();
            }
        }
        return diff;
    }

    private void setOffline() {
        if (!this.online) {
            DbOffline.setGenerateMigration();
            if (this.databasePlatform == null || !this.platforms.isEmpty()) {
                this.setPlatform(DbPlatformName.H2);
            }
        }
    }

    @Override
    public void generateMigration() throws IOException {
        setOffline();
        try {
            if (scriptInfo != null) {
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
            scriptInfo = server.find(ScriptInfo.class, version);
            if (scriptInfo != null) {
                return false;
            }
        }
        scriptInfo = new ScriptInfo();
        scriptInfo.setRevision(version);
        try (StringWriter writer = new StringWriter()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(Migration.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(dbMigration, writer);
            writer.flush();
            scriptInfo.setModelDiff(writer.toString());
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    protected PlatformDdlWriter createDdlWriter() {
        return new PlatformDdlWriter(scriptInfo, (SpiServer) server);
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
        scriptInfo = null;
        return old;
    }

    public ScriptInfo getScriptInfo() {
        return scriptInfo;
    }
}
