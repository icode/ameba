package ameba.db.migration;

import ameba.db.migration.models.MigrationInfo;
import org.jvnet.hk2.annotations.Contract;

import java.util.List;

/**
 * @author icode
 */
@Contract
public interface Migration {
    boolean hasChanged();

    List<MigrationInfo> generate();

    void persist();

    void reset();
}
