package ameba.db.ebean.migration;

import ameba.db.migration.models.ScriptInfo;
import com.google.common.collect.Lists;
import io.ebean.Transaction;
import io.ebean.annotation.TxIsolation;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.model.MigrationVersion;
import io.ebeaninternal.dbmigration.model.ModelContainer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

/**
 * <p>MigrationModel class.</p>
 *
 * @author icode
 */
public class MigrationModel {
    private static final Logger logger = LoggerFactory.getLogger(MigrationModel.class);
    private final ModelContainer model = new ModelContainer();
    private final SpiEbeanServer server;
    private boolean migrationTableExist;

    private MigrationVersion lastVersion;

    /**
     * <p>Constructor for MigrationModel.</p>
     *
     * @param server a {@link io.ebeaninternal.api.SpiEbeanServer} object.
     */
    public MigrationModel(SpiEbeanServer server) {
        this.server = server;
    }

    /**
     * <p>read.</p>
     *
     * @return a {@link io.ebeaninternal.dbmigration.model.ModelContainer} object.
     */
    public ModelContainer read() {
        readMigrations();
        return model;
    }

    /**
     * <p>readMigrations.</p>
     */
    protected void readMigrations() {
        final List<MigrationResource> resources = findMigrationResource();

        // sort into version order before applying
        Collections.sort(resources);

        for (MigrationResource migrationResource : resources) {
            logger.debug("read {}", migrationResource);
            model.apply(migrationResource.read(), migrationResource.getVersion());
        }

        // remember the last version
        if (!resources.isEmpty()) {
            lastVersion = resources.get(resources.size() - 1).getVersion();
        }
    }

    /**
     * <p>findMigrationResource.</p>
     *
     * @return a {@link java.util.List} object.
     */
    protected List<MigrationResource> findMigrationResource() {
        final List<MigrationResource> resources = Lists.newArrayList();
        BeanDescriptor<ScriptInfo> beanDescriptor = server.getBeanDescriptor(ScriptInfo.class);
        Transaction transaction = server.createTransaction(TxIsolation.READ_COMMITED);
        try (Connection connection = transaction.getConnection()) {
            String tableName = beanDescriptor.getBaseTable();
            ResultSet rs = connection.getMetaData()
                    .getTables(null, null, "%", null);
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString(3))) {
                    migrationTableExist = true;
                    break;
                }
            }
            if (migrationTableExist) {
                server.find(ScriptInfo.class)
                        .findEach(bean -> resources.add(new MigrationResource(bean)));
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            transaction.end();
        }
        return resources;
    }

    /**
     * <p>getNextVersion.</p>
     *
     * @param initialVersion a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getNextVersion(String initialVersion) {
        return lastVersion == null ? initialVersion : lastVersion.nextVersion();
    }

    public boolean isMigrationTableExist() {
        return migrationTableExist;
    }
}
