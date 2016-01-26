package ameba.db.migration;

import ameba.db.migration.models.ScriptInfo;
import org.jvnet.hk2.annotations.Contract;

import java.util.List;

/**
 * @author icode
 */
@Contract
public interface Migration {
    boolean hasChanged();

    ScriptInfo generate();

    List<ScriptInfo> allScript();

    ScriptInfo getScript(String revision);

    void persist();

    void reset();
}
