package ameba.db.ebean.migration;

import ameba.core.Application;
import ameba.db.migration.Migration;
import ameba.db.migration.models.ScriptInfo;
import ameba.exception.AmebaException;
import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

/**
 * <p>EbeanMigration class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class EbeanMigration implements Migration {
    private final ModelMigration dbMigration;
    private final SpiEbeanServer server;

    /**
     * <p>Constructor for EbeanMigration.</p>
     *
     * @param application a {@link ameba.core.Application} object.
     * @param server      a {@link com.avaje.ebeaninternal.api.SpiEbeanServer} object.
     */
    public EbeanMigration(Application application, SpiEbeanServer server) {
        boolean isDev = application.getMode().isDev();
        this.server = server;
        String _basePath = (isDev ? "src/main" : "temp") + "/";
        DbMigrationConfig migrationConfig = new DbMigrationConfig();
        ServerConfig config = server.getServerConfig();
        CharSequence ver = application.getApplicationVersion();
        String version;
        String verIndex = DateTime.now().toString("yyyyMMddHHmmss");
        if (ver instanceof Application.UnknownVersion) {
            version = verIndex;
        } else {
            version = String.valueOf(ver) + "_" + verIndex;
        }
        migrationConfig.setVersion(version);
        migrationConfig.setMigrationPath("ameba/db/migration/" + server.getName());
        config.setMigrationConfig(migrationConfig);

        dbMigration = new ModelMigration();
        dbMigration.setPlatform(server.getDatabasePlatform());
        dbMigration.setServer(server);
        dbMigration.setPathToResources(_basePath);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChanged() {
        return !dbMigration.diff().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public ScriptInfo generate() {
        try {
            dbMigration.generateMigration();
        } catch (IOException e) {
            throw new AmebaException(e);
        }
        return dbMigration.getScriptInfo();
    }

    /** {@inheritDoc} */
    @Override
    public List<ScriptInfo> allScript() {
        final List<ScriptInfo> scriptInfoList = Lists.newArrayList();
        server.find(ScriptInfo.class).findEach(new QueryEachConsumer<ScriptInfo>() {
            @Override
            public void accept(ScriptInfo bean) {
                scriptInfoList.add(bean);
            }
        });
        return scriptInfoList;
    }

    /** {@inheritDoc} */
    @Override
    public ScriptInfo getScript(String revision) {
        return server.find(ScriptInfo.class, revision);
    }

    /** {@inheritDoc} */
    @Override
    public void persist() {
        server.save(dbMigration.getScriptInfo());
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        dbMigration.rest();
    }
}
