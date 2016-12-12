package conf

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import org.apache.commons.lang3.StringUtils

import static ch.qos.logback.classic.Level.*

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}


Properties properties = context.getObject("properties");
String trace = properties.getProperty("ameba.trace.enabled");
boolean isTrace = "true".equalsIgnoreCase(trace);
String appPackage = properties.getProperty("app.package");

logger("org.glassfish", isTrace ? TRACE : WARN)
logger("org.glassfish.jersey.filter", INFO)
logger("org.glassfish.jersey.message.internal", isTrace ? TRACE : OFF)
logger("org.glassfish.jersey.server.ServerRuntime\$Responder", isTrace ? TRACE : OFF)

logger("io.ebean.SQL", TRACE)
logger("io.ebean.TXN", TRACE)
logger("io.ebean.SUM", TRACE)
logger("io.ebean.cache.QUERY", TRACE)
logger("io.ebean.cache.BEAN", TRACE)
logger("io.ebean.cache.COLL", TRACE)
logger("io.ebean.cache.NATKEY", TRACE)
logger("io.ebean.Cluster", DEBUG)
logger("io.ebeaninternal.server.transaction", isTrace ? TRACE : DEBUG)
logger("io.ebeaninternal.server.lib", DEBUG)
logger("io.ebeaninternal.server.deploy.BeanDescriptor", isTrace ? TRACE : DEBUG)
logger("io.ebeaninternal.server.deploy.BeanDescriptorManager", WARN)

logger("ameba", isTrace ? TRACE : DEBUG)

if (appPackage != null) {
    for (String pkg : appPackage.split(",")) {
        if (StringUtils.isNotBlank(pkg))
            logger(pkg, TRACE)
    }
}

root(WARN, ["CONSOLE"])