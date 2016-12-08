package ameba.db.migration.flyway;

import ameba.db.migration.Migration;
import ameba.db.migration.models.ScriptInfo;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

/**
 * <p>DatabaseMigrationResolver class.</p>
 *
 * @author icode
 *
 */
public class DatabaseMigrationResolver implements MigrationResolver {

    private Migration migration;

    /**
     * <p>Constructor for DatabaseMigrationResolver.</p>
     *
     * @param migration a {@link ameba.db.migration.Migration} object.
     */
    public DatabaseMigrationResolver(Migration migration) {
        this.migration = migration;
    }

    static int calculateChecksum(String str) {
        Hasher hasher = Hashing.murmur3_32().newHasher();

        BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                hasher.putString(line.trim(), Charsets.UTF_8);
            }
        } catch (IOException e) {
            String message = "Unable to calculate checksum";
            throw new FlywayException(message, e);
        }

        return hasher.hash().asInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> resolvedMigrations = Lists.newArrayList();
        ScriptInfo info = migration.generate();
        if (info != null) {
            ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
            migration.setVersion(MigrationVersion.fromVersion(info.getRevision()));
            migration.setDescription(info.getDescription());
            migration.setScript(info.getRevision() + "__" + toUnderScore(info.getDescription()) + ".sql");
            migration.setChecksum(calculateChecksum("-- " + info.getRevision() + "\r\n" + info.getApplyDdl()));
            migration.setType(MigrationType.SQL);
            migration.setExecutor(new SqlMigrationExecutor(info));
            resolvedMigrations.add(migration);
        }
        return resolvedMigrations;
    }


    private String toUnderScore(String name) {
        return name.replace(' ', '_');
    }
}
