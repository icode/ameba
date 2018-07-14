package ameba.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract LoggerOwner class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public abstract class LoggerOwner {
    /**
     * <p>logger.</p>
     *
     * @return a {@link org.slf4j.Logger} object.
     */
    protected Logger logger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
