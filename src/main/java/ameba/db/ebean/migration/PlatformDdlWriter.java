package ameba.db.ebean.migration;

import ameba.db.migration.models.MigrationInfo;
import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.plugin.SpiServer;

import java.io.IOException;
import java.util.List;

/**
 * @author icode
 */
public class PlatformDdlWriter {

    private final MigrationInfo migrationInfo;
    private final SpiServer server;
    private final DbMigrationConfig config;

    public PlatformDdlWriter(MigrationInfo migrationInfo, DbMigrationConfig config, SpiServer server) {
        this.migrationInfo = migrationInfo;
        this.server = server;
        this.config = config;
    }

    public void processMigration(Migration dbMigration, DdlWrite write) throws IOException {
        DdlHandler handler = handler();

        List<ChangeSet> changeSets = dbMigration.getChangeSet();
        for (ChangeSet changeSet : changeSets) {
            if (!changeSet.getChangeSetChildren().isEmpty()) {
                handler.generate(write, changeSet);
            }
        }
        handler.generateExtra(write);

        writePlatformDdl(write);
    }

    protected void writePlatformDdl(DdlWrite write) throws IOException {

        if (!write.isApplyEmpty()) {
            writeApplyDdl(write);

            if (!config.isSuppressRollback() && !write.isApplyRollbackEmpty()) {
                writeApplyRollbackDdl(write);
            }
        }

        if (!write.isDropEmpty()) {
            writeDropDdl(write);
        }
    }

    /**
     * Write the 'Apply' DDL buffers to the writer.
     */
    protected void writeApplyDdl(DdlWrite write) throws IOException {
        migrationInfo.setApplyDdl(
                write.apply().getBuffer() +
                        write.applyForeignKeys().getBuffer() +
                        write.applyHistory().getBuffer()
        );
    }

    /**
     * Write the 'Rollback' DDL buffers to the writer.
     */
    protected void writeApplyRollbackDdl(DdlWrite write) throws IOException {
        migrationInfo.setRollbackDdl(
                write.rollbackForeignKeys().getBuffer() +
                        write.rollback().getBuffer()
        );
    }

    /**
     * Write the 'Drop' DDL buffers to the writer.
     */
    protected void writeDropDdl(DdlWrite write) throws IOException {
        migrationInfo.setDropDdl(
                write.dropHistory().getBuffer() +
                        write.drop().getBuffer()
        );
    }

    /**
     * Return the platform specific DdlHandler (to generate DDL).
     */
    protected DdlHandler handler() {
        return server.getDatabasePlatform().createDdlHandler(server.getServerConfig());
    }
}
