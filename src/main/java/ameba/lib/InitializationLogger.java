package ameba.lib;

import ameba.core.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * @author icode
 */
public class InitializationLogger implements Logger {
    private final Logger logger;
    private final Application application;

    public InitializationLogger(Class clazz, Application application) {
        this.application = application;
        logger = LoggerFactory.getLogger(clazz);
    }

    public Logger getSource() {
        return logger;
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String s) {
        if (!isInitialized())
            logger.trace(s);
    }

    @Override
    public void trace(String s, Object o) {
        if (!isInitialized())
            logger.trace(s, o);
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        if (!isInitialized())
            logger.trace(s, o, o1);
    }

    @Override
    public void trace(String s, Object... objects) {
        if (!isInitialized())
            logger.trace(s, objects);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        if (!isInitialized())
            logger.trace(s, throwable);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String s) {
        if (!isInitialized())
            logger.trace(marker, s);
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        if (!isInitialized())
            logger.trace(marker, s, o);
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        if (!isInitialized())
            logger.trace(marker, s, o, o1);
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        if (!isInitialized())
            logger.trace(marker, s, objects);
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        if (!isInitialized())
            logger.trace(marker, s, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String s) {
        if (!isInitialized())
            logger.debug(s);
    }

    @Override
    public void debug(String s, Object o) {
        if (!isInitialized())
            logger.debug(s, o);
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        if (!isInitialized())
            logger.debug(s, o, o1);
    }

    @Override
    public void debug(String s, Object... objects) {
        if (!isInitialized())
            logger.debug(s, objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        if (!isInitialized())
            logger.debug(s, throwable);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String s) {
        if (!isInitialized())
            logger.debug(marker, s);
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        if (!isInitialized())
            logger.debug(marker, s, o);
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        if (!isInitialized())
            logger.debug(marker, s, o, o1);
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        if (!isInitialized())
            logger.debug(marker, s, objects);
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        if (!isInitialized())
            logger.debug(marker, s, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String s) {
        if (!isInitialized())
            logger.info(s);
    }

    @Override
    public void info(String s, Object o) {
        if (!isInitialized())
            logger.info(s, o);
    }

    @Override
    public void info(String s, Object o, Object o1) {
        if (!isInitialized())
            logger.info(s, o, o1);
    }

    @Override
    public void info(String s, Object... objects) {
        if (!isInitialized())
            logger.info(s, objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        if (!isInitialized())
            logger.info(s, throwable);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String s) {
        if (!isInitialized())
            logger.info(marker, s);
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        if (!isInitialized())
            logger.info(marker, s, o);
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        if (!isInitialized())
            logger.info(marker, s, o, o1);
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        if (!isInitialized())
            logger.info(marker, s, objects);
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        if (!isInitialized())
            logger.info(marker, s, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String s) {
        if (!isInitialized())
            logger.warn(s);
    }

    @Override
    public void warn(String s, Object o) {
        if (!isInitialized())
            logger.warn(s, o);
    }

    @Override
    public void warn(String s, Object... objects) {
        if (!isInitialized())
            logger.warn(s, objects);
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        if (!isInitialized())
            logger.warn(s, o, o1);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        if (!isInitialized())
            logger.warn(s, throwable);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String s) {
        if (!isInitialized())
            logger.warn(marker, s);
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        if (!isInitialized())
            logger.warn(marker, s, o);
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        if (!isInitialized())
            logger.warn(marker, s, o, o1);
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        if (!isInitialized())
            logger.warn(marker, s, objects);
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        if (!isInitialized())
            logger.warn(marker, s, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String s) {
        if (!isInitialized())
            logger.error(s);
    }

    @Override
    public void error(String s, Object o) {
        if (!isInitialized())
            logger.error(s, o);
    }

    @Override
    public void error(String s, Object o, Object o1) {
        if (!isInitialized())
            logger.error(s, o, o1);
    }

    @Override
    public void error(String s, Object... objects) {
        if (!isInitialized())
            logger.error(s, objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        if (!isInitialized())
            logger.error(s, throwable);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String s) {
        if (!isInitialized())
            logger.error(marker, s);
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        if (!isInitialized())
            logger.error(marker, s, o);
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        if (!isInitialized())
            logger.error(marker, s, o, o1);
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        if (!isInitialized())
            logger.error(marker, s, objects);
    }

    private boolean isInitialized() {
        return application != null && application.isInitialized();
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        if (!isInitialized())
            logger.error(marker, s, throwable);
    }
}
