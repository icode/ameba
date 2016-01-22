package ameba.db.migration;

import ameba.core.Application;
import ameba.db.DataSourceManager;
import ameba.db.migration.flyway.DatabaseMigrationResolver;
import ameba.i18n.Messages;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.flywaydb.core.Flyway;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.Map;

/**
 * @author icode
 */
public class MigrationFeature implements Feature {

    // 随机生成的初始化数据库页面的uri
    static final String uri = "@db/migration/" + RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(20, 30));
    static final Map<String, Migration> migrationMap = Maps.newHashMap();
    private static final Logger logger = LoggerFactory.getLogger(MigrationFeature.class);
    private static final Map<String, Flyway> FLYWAYS = Maps.newHashMap();
    @Inject
    private ServiceLocator locator;
    @Inject
    private Application.Mode mode;

    public static Flyway getMigration(String dbName) {
        return FLYWAYS.get(dbName);
    }

    @Override
    public boolean configure(FeatureContext context) {
        Map<String, Object> properties = context.getConfiguration().getProperties();
        for (String dbName : DataSourceManager.getDataSourceNames()) {
            if (!"false".equals(properties.get("db." + dbName + ".migration.enabled"))) {
                Flyway flyway = new Flyway();
                flyway.setDataSource(DataSourceManager.getDataSource(dbName));
                flyway.setBaselineOnMigrate(true);
                FLYWAYS.put(dbName, flyway);
                Migration migration = locator.getService(Migration.class, dbName);
                migrationMap.put(dbName, migration);
                flyway.setResolvers(new DatabaseMigrationResolver(migration));
            }
        }

        if (!migrationMap.isEmpty()) {
            context.register(MigrationFilter.class);
        }

        if (!mode.isDev()) {
            for (Migration migration : migrationMap.values()) {
                if (migration.hasChanged()) {
                    logger.warn(Messages.get("warn.app.database.migration", uri));
                    break;
                }
            }
        }

        return true;
    }
}
