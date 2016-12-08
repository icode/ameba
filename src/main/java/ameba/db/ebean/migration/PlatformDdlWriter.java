package ameba.db.ebean.migration;

import ameba.db.migration.models.ScriptInfo;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.ChangeSetType;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.plugin.SpiServer;

import java.io.IOException;
import java.util.List;

/**
 * <p>PlatformDdlWriter class.</p>
 *
 * @author icode
 *
 */
public class PlatformDdlWriter {

    private final ScriptInfo scriptInfo;
    private final SpiServer server;

    /**
     * <p>Constructor for PlatformDdlWriter.</p>
     *
     * @param scriptInfo a {@link ameba.db.migration.models.ScriptInfo} object.
     * @param server     a {@link com.avaje.ebean.plugin.SpiServer} object.
     */
    public PlatformDdlWriter(ScriptInfo scriptInfo, SpiServer server) {
        this.scriptInfo = scriptInfo;
        this.server = server;
    }

    /**
     * <p>processMigration.</p>
     *
     * @param dbMigration a {@link com.avaje.ebean.dbmigration.migration.Migration} object.
     * @param write a {@link com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite} object.
     * @throws java.io.IOException if any.
     */
    public void processMigration(Migration dbMigration, DdlWrite write) throws IOException {
        DdlHandler handler = handler();

        List<ChangeSet> changeSets = dbMigration.getChangeSet();
        for (ChangeSet changeSet : changeSets) {
            if (isApply(changeSet)) {
                handler.generate(write, changeSet);
            }
        }
        handler.generateExtra(write);

        writePlatformDdl(write);
    }

    /**
     * Return true if the changeSet is APPLY and not empty.
     */
    private boolean isApply(ChangeSet changeSet) {
        // 必须包含　PENDING_DROPS　不然无法在只删除列时产生变更脚本
        return (changeSet.getType() == ChangeSetType.APPLY || changeSet.getType() == ChangeSetType.PENDING_DROPS) && !changeSet.getChangeSetChildren().isEmpty();
    }

    /**
     * <p>writePlatformDdl.</p>
     *
     * @param write a {@link com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite} object.
     * @throws java.io.IOException if any.
     */
    protected void writePlatformDdl(DdlWrite write) throws IOException {
        if (!write.isApplyEmpty()) {
            writeApplyDdl(write);
        }
    }

    /**
     * Write the 'Apply' DDL buffers to the writer.
     *
     * @param write a {@link com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite} object.
     * @throws java.io.IOException if any.
     */
    protected void writeApplyDdl(DdlWrite write) throws IOException {
        scriptInfo.setApplyDdl(
                "-- drop dependencies\n"
                        + write.applyDropDependencies().getBuffer() + "\n"
                        + "-- apply changes\n"
                        + write.apply().getBuffer()
                        + write.applyForeignKeys().getBuffer()
                        + write.applyHistory().getBuffer()
        );
    }

    /**
     * Return the platform specific DdlHandler (to generate DDL).
     *
     * @return a {@link com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler} object.
     */
    protected DdlHandler handler() {
        return server.getDatabasePlatform().createDdlHandler(server.getServerConfig());
    }
}
