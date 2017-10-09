package ameba.db.ebean.migration;

import ameba.db.migration.models.ScriptInfo;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.dbmigration.DbOffline;
import io.ebeaninternal.dbmigration.DefaultDbMigration;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import io.ebeaninternal.dbmigration.model.MConfiguration;
import io.ebeaninternal.dbmigration.model.ModelContainer;
import io.ebeaninternal.dbmigration.model.ModelDiff;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * <p>ModelMigration class.</p>
 *
 * @author icode
 */
public class ModelMigration extends DefaultDbMigration {
    private static final String initialVersion = "1.0";
    private ModelDiff diff;
    private MigrationModel migrationModel;
    private CurrentModel currentModel;
    private ScriptInfo scriptInfo;

    /**
     * <p>diff.</p>
     *
     * @return a {@link io.ebeaninternal.dbmigration.model.ModelDiff} object.
     */
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
        } finally {
            if (!online) {
                DbOffline.reset();
            }
        }
        return diff;
    }

    private void setOffline() {
        if (!online) {
            DbOffline.setGenerateMigration();
            if (databasePlatform == null && !platforms.isEmpty()) {
                // for multiple platform generation set the general platform
                // to H2 so that it runs offline without DB connection
                setPlatform(platforms.get(0).platform);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateMigration() throws IOException {
        if (scriptInfo != null) {
            return null;
        }
        if (diff == null) {
            diff();
        }

        setOffline();
        String version = null;
        try {
            if (diff.isEmpty()) {
                logger.info("no changes detected - no migration written");
                return null;
            }
            // there were actually changes to write
            Migration dbMigration = diff.getMigration();

            version = getVersion(migrationModel);
            logger.info("generating migration:{}", version);
            if (!writeMigration(dbMigration, version)) {
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
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeExtraPlatformDdl(String fullVersion, CurrentModel currentModel,
                                         Migration dbMigration, File writePath) throws IOException {
        throw new UnsupportedOperationException("writeExtraPlatformDdl Unsupported");
    }

    /**
     * {@inheritDoc}
     */
    protected boolean writeMigrationXml(Migration dbMigration, File resourcePath, String fullVersion) {
        throw new UnsupportedOperationException("writeExtraPlatformDdl Unsupported");
    }

    /**
     * <p>writeMigration.</p>
     *
     * @param dbMigration a {@link io.ebeaninternal.dbmigration.migration.Migration} object.
     * @param version     a {@link java.lang.String} object.
     * @return a boolean.
     */
    protected boolean writeMigration(Migration dbMigration, String version) {
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

    /**
     * <p>createDdlWriter.</p>
     *
     * @return a {@link ameba.db.ebean.migration.PlatformDdlWriter} object.
     */
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

    /**
     * <p>rest.</p>
     *
     * @return a {@link io.ebeaninternal.dbmigration.model.ModelDiff} object.
     */
    public ModelDiff rest() {
        ModelDiff old = diff;
        diff = null;
        migrationModel = null;
        currentModel = null;
        scriptInfo = null;
        return old;
    }

    /**
     * <p>Getter for the field <code>scriptInfo</code>.</p>
     *
     * @return a {@link ameba.db.migration.models.ScriptInfo} object.
     */
    public ScriptInfo getScriptInfo() {
        return scriptInfo;
    }
}
