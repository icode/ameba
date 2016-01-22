package ameba.db.migration;

import ameba.db.migration.models.MigrationInfo;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author icode
 */
@Contract
public interface Migration {
    boolean hasChanged();

    MigrationInfo generate();

    void persist();

    void reset();
}
