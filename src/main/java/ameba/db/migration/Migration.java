package ameba.db.migration;

import ameba.db.migration.models.ScriptInfo;
import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import java.util.List;

/**
 * @author icode
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface Migration {
    boolean hasChanged();

    ScriptInfo generate();

    List<ScriptInfo> allScript();

    ScriptInfo getScript(String revision);

    void persist();

    void reset();
}
