package ameba.lib;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <p>Abstract LoggerOwner class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
public abstract class LoggerOwner {
    private static final Map<Class, Logger> LOGGER_MAP = Maps.newConcurrentMap();

    /**
     * <p>logger.</p>
     *
     * @return a {@link org.slf4j.Logger} object.
     */
    protected Logger logger() {
        Class thisClass = this.getClass();
        Logger logger = LOGGER_MAP.get(thisClass);
        if (logger == null) {
            logger = LoggerFactory.getLogger(thisClass);
            LOGGER_MAP.put(thisClass, logger);
        }
        return logger;
    }
}
