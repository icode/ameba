package ameba.db.ebean.migration;

import ameba.db.migration.models.ScriptInfo;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlHandler;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.ChangeSetType;
import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.server.core.PlatformDdlBuilder;

import java.io.IOException;
import java.util.List;

/**
 * <p>PlatformDdlWriter class.</p>
 *
 * @author icode
 */
public class PlatformDdlWriter {

    private final ScriptInfo scriptInfo;
    private final SpiServer server;
    private final PlatformDdl platformDdl;

    /**
     * <p>Constructor for PlatformDdlWriter.</p>
     *
     * @param scriptInfo a {@link ameba.db.migration.models.ScriptInfo} object.
     * @param server     a {@link io.ebean.plugin.SpiServer} object.
     */
    public PlatformDdlWriter(ScriptInfo scriptInfo, SpiServer server) {
        this.scriptInfo = scriptInfo;
        this.server = server;
        this.platformDdl = PlatformDdlBuilder.create(server.getDatabasePlatform());
    }

    /**
     * <p>processMigration.</p>
     *
     * @param dbMigration a {@link io.ebeaninternal.dbmigration.migration.Migration} object.
     * @param write       a {@link io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite} object.
     * @throws java.io.IOException if any.
     */
    public void processMigration(Migration dbMigration, DdlWrite write) throws IOException {
        DdlHandler handler = handler();
        handler.generateProlog(write);
        List<ChangeSet> changeSets = dbMigration.getChangeSet();
        for (ChangeSet changeSet : changeSets) {
            if (isApply(changeSet)) {
                handler.generate(write, changeSet);
            }
        }
        handler.generateEpilog(write);

        writePlatformDdl(write);
    }

    /**
     * Return true if the changeSet is APPLY and not empty.
     */
    private boolean isApply(ChangeSet changeSet) {
        // 必须包含　PENDING_DROPS　不然无法在只删除列时产生变更脚本
        return (changeSet.getType() == ChangeSetType.APPLY || changeSet.getType() == ChangeSetType.PENDING_DROPS)
                && !changeSet.getChangeSetChildren().isEmpty();
    }

    /**
     * <p>writePlatformDdl.</p>
     *
     * @param write a {@link io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite} object.
     */
    protected void writePlatformDdl(DdlWrite write) {
        if (!write.isApplyEmpty()) {
            writeApplyDdl(write);
        }
    }

    /**
     * Write the 'Apply' DDL buffers to the writer.
     *
     * @param write a {@link io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite} object.
     * @throws java.io.IOException if any.
     */
    protected void writeApplyDdl(DdlWrite write) {
        scriptInfo.setApplyDdl(
                "-- drop dependencies\n"
                        + write.applyDropDependencies().getBuffer() + "\n"
                        + "-- apply changes\n"
                        + write.apply().getBuffer()
                        + write.applyForeignKeys().getBuffer()
                        + write.applyHistoryView().getBuffer()
                        + write.applyHistoryTrigger().getBuffer()
        );
    }

    /**
     * Return the platform specific DdlHandler (to generate DDL).
     *
     * @return a {@link io.ebeaninternal.dbmigration.ddlgeneration.DdlHandler} object.
     */
    protected DdlHandler handler() {
        return platformDdl.createDdlHandler(server.getServerConfig());
    }
}
