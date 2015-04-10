package conf

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.*

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}


String appPackage = context.getProperty("appPackage");
String trace = context.getProperty("ameba.trace.enabled");
boolean isTrace = "true".equalsIgnoreCase(trace);

logger("org.glassfish.jersey.filter.LoggingFilter", INFO)
logger("org.glassfish", WARN)

logger("org.avaje.ebean.SQL", TRACE)
logger("org.avaje.ebean.TXN", TRACE)
logger("org.avaje.ebean.SUM", TRACE)
logger("org.avaje.ebean.cache.QUERY", isTrace ? TRACE : DEBUG)
logger("org.avaje.ebean.cache.BEAN", isTrace ? TRACE : DEBUG)
logger("org.avaje.ebean.cache.COLL", isTrace ? TRACE : DEBUG)
logger("org.avaje.ebean.cache.NATKEY", isTrace ? TRACE : DEBUG)
logger("com.avaje.ebeaninternal.server.lib.sql", isTrace ? TRACE : DEBUG)
logger("com.avaje.ebeaninternal.server.transaction", isTrace ? TRACE : DEBUG)
logger("com.avaje.ebeaninternal.server.cluster", DEBUG)
logger("com.avaje.ebeaninternal.server.lib", DEBUG)
logger("com.avaje.ebeaninternal.server.deploy.BeanDescriptor", isTrace ? TRACE : DEBUG)

logger("httl", isTrace ? DEBUG : WARN)
logger("ameba", isTrace ? TRACE : DEBUG)
if (appPackage != null)
    logger(appPackage, TRACE)
root(WARN, ["CONSOLE"])