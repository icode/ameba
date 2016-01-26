package ameba.db.migration.flyway;

import ameba.db.migration.models.ScriptInfo;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.SqlScript;

import java.sql.Connection;

/**
 * @author icode
 */
public class SqlMigrationExecutor implements MigrationExecutor {
    private final ScriptInfo info;

    public SqlMigrationExecutor(ScriptInfo info) {
        this.info = info;
    }

    public void execute(Connection connection) {
        SqlScript sqlScript = new SqlScript(info.getDiffDdl(), DbSupportFactory.createDbSupport(connection, true));
        sqlScript.execute(new JdbcTemplate(connection, 0));
    }

    public boolean executeInTransaction() {
        return true;
    }
}