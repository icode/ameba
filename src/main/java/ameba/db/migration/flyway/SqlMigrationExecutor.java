package ameba.db.migration.flyway;

import ameba.db.migration.models.ScriptInfo;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.SqlScript;

import java.sql.Connection;

/**
 * <p>SqlMigrationExecutor class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class SqlMigrationExecutor implements MigrationExecutor {
    private final ScriptInfo info;

    /**
     * <p>Constructor for SqlMigrationExecutor.</p>
     *
     * @param info a {@link ameba.db.migration.models.ScriptInfo} object.
     */
    public SqlMigrationExecutor(ScriptInfo info) {
        this.info = info;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Connection connection) {
        SqlScript sqlScript = new SqlScript(info.getApplyDdl(), DbSupportFactory.createDbSupport(connection, true));
        sqlScript.execute(new JdbcTemplate(connection, 0));
    }

    /**
     * <p>executeInTransaction.</p>
     *
     * @return a boolean.
     */
    public boolean executeInTransaction() {
        return true;
    }
}
