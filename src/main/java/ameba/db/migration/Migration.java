package ameba.db.migration;

import ameba.db.migration.models.ScriptInfo;
import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import java.util.List;

/**
 * <p>Migration interface.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface Migration {
    /**
     * <p>hasChanged.</p>
     *
     * @return a boolean.
     */
    boolean hasChanged();

    /**
     * <p>generate.</p>
     *
     * @return a {@link ameba.db.migration.models.ScriptInfo} object.
     */
    ScriptInfo generate();

    /**
     * <p>allScript.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<ScriptInfo> allScript();

    /**
     * <p>getScript.</p>
     *
     * @param revision a {@link java.lang.String} object.
     * @return a {@link ameba.db.migration.models.ScriptInfo} object.
     */
    ScriptInfo getScript(String revision);

    /**
     * <p>persist.</p>
     */
    void persist();

    /**
     * <p>reset.</p>
     */
    void reset();
}
