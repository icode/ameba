package ameba.db.ebean.migration;

import ameba.db.migration.models.MigrationInfo;
import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxIsolation;
import com.avaje.ebean.dbmigration.model.MigrationVersion;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

/**
 * @author icode
 */
public class MigrationModel {
    private static final Logger logger = LoggerFactory.getLogger(MigrationModel.class);
    private final ModelContainer model = new ModelContainer();
    private final SpiEbeanServer server;
    private boolean migrationTableExist;

    private MigrationVersion lastVersion;

    public MigrationModel(SpiEbeanServer server) {
        this.server = server;
    }

    public ModelContainer read() {
        readMigrations();
        return model;
    }

    protected void readMigrations() {
        final List<MigrationResource> resources = findMigrationResource();

        // sort into version order before applying
        Collections.sort(resources);

        for (MigrationResource migrationResource : resources) {
            logger.debug("read {}", migrationResource);
            model.apply(migrationResource.read());
        }

        // remember the last version
        if (!resources.isEmpty()) {
            lastVersion = resources.get(resources.size() - 1).getVersion();
        }
    }

    protected List<MigrationResource> findMigrationResource() {
        final List<MigrationResource> resources = Lists.newArrayList();
        BeanDescriptor<MigrationInfo> beanDescriptor = server.getBeanDescriptor(MigrationInfo.class);
        Transaction transaction = server.createTransaction(TxIsolation.READ_COMMITED);
        try (Connection connection = transaction.getConnection()) {
            ResultSet resultSet = connection.getMetaData().getTables(
                    null, null, beanDescriptor.getBaseTable(), null
            );
            migrationTableExist = resultSet.next();
            if (migrationTableExist) {
                server.find(MigrationInfo.class)
                        .where()
                        .eq("databaseName", server.getName())
                        .findEach(new QueryEachConsumer<MigrationInfo>() {
                            @Override
                            public void accept(MigrationInfo bean) {
                                resources.add(new MigrationResource(bean));
                            }
                        });
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            transaction.end();
        }
        return resources;
    }

    public String getNextVersion(String initialVersion) {
        return lastVersion == null ? initialVersion : lastVersion.nextVersion();
    }

    public boolean isMigrationTableExist() {
        return migrationTableExist;
    }
}
