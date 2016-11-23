package ameba.db.migration;

import ameba.core.Application;
import ameba.db.DataSourceManager;
import ameba.db.migration.flyway.DatabaseMigrationResolver;
import ameba.db.migration.resources.MigrationResource;
import ameba.i18n.Messages;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.flywaydb.core.Flyway;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.List;
import java.util.Map;

/**
 * <p>MigrationFeature class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class MigrationFeature implements Feature {

    private static final Logger logger = LoggerFactory.getLogger(MigrationFeature.class);
    // 随机生成的初始化数据库页面的id
    private static String MIGRATION_ID;
    @Inject
    private ServiceLocator locator;
    @Inject
    private Application.Mode mode;

    /**
     * <p>checkMigrationId.</p>
     *
     * @param uuid a {@link java.lang.String} object.
     */
    public static void checkMigrationId(String uuid) {
        if (!MIGRATION_ID.equals(uuid)) {
            throw new NotFoundException();
        }
    }

    /**
     * <p>getMigrationId.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getMigrationId() {
        return MIGRATION_ID;
    }


    /**
     * <p>generateMigrationId.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String generateMigrationId() {
        return MIGRATION_ID = RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(20, 30));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {
        generateMigrationId();
        Map<String, Object> properties = context.getConfiguration().getProperties();
        List<Migration> migrations = Lists.newArrayList();
        for (String dbName : DataSourceManager.getDataSourceNames()) {
            if (!"false".equals(properties.get("db." + dbName + ".migration.enabled"))) {
                Flyway flyway = new Flyway();
                flyway.setDataSource(DataSourceManager.getDataSource(dbName));
                flyway.setBaselineOnMigrate(true);
                bindFlyway(dbName, flyway);
                Migration migration = locator.getService(Migration.class, dbName);
                migrations.add(migration);
                flyway.setResolvers(new DatabaseMigrationResolver(migration));
            }
        }

        if (!migrations.isEmpty()) {
            context.register(MigrationFilter.class);
        }

        context.register(MigrationResource.class);

        if (!mode.isDev()) {
            for (Migration migration : migrations) {
                if (migration.hasChanged()) {
                    logger.warn(Messages.get("warn.app.database.migration", "@db/migration/" + MIGRATION_ID));
                    break;
                }
            }
        }

        return true;
    }

    private void bindFlyway(final String name, final Flyway flyway) {
        ServiceLocatorUtilities.bind(locator, new AbstractBinder() {
            @Override
            protected void configure() {
                bind(flyway).to(Flyway.class).named(name).proxy(false);
                if (name.equals(DataSourceManager.getDefaultDataSourceName())) {
                    bind(flyway).to(Flyway.class).proxy(false);
                }
            }
        });
    }
}
