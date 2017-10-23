package conf

import ameba.core.Application
import ameba.util.IOUtils
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import org.apache.commons.lang3.StringUtils

import static ch.qos.logback.classic.Level.*

String PATTERN = "%d{HH:mm:ss.SSS} %boldYellow([%thread]) %highlight(%-5level) %boldGreen(%logger{36}) - %msg%n"

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = PATTERN
    }
}
Properties properties = context.getObject("properties")
String appName = properties.getProperty("app.name")
if (StringUtils.isBlank(appName)) {
    appName = Application.DEFAULT_APP_NAME
}
String trace = properties.getProperty("ameba.trace.enabled")
boolean isTrace = "true".equalsIgnoreCase(trace)
String appPackage = properties.getProperty("app.package")

appender("FILE", RollingFileAppender) {
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = IOUtils.getResource("").getFile() + "logs/" + appName + ".%d{yyyy-MM-dd}-%i.log"
        maxHistory = 30
        timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
            maxFileSize = "20MB"
        }
    }
    encoder(PatternLayoutEncoder) {
        pattern = PATTERN
    }
}

if (appPackage != null) {
    for (String pkg : appPackage.split(",")) {
        if (StringUtils.isNotBlank(pkg))
            logger(pkg, TRACE)
    }
}

logger("org.glassfish", WARN)
logger("org.glassfish.jersey.filter", INFO)
logger("org.glassfish.jersey.message.internal", OFF)
logger("org.glassfish.jersey.server.ServerRuntime\$Responder", OFF)
logger("io.ebean", WARN)
logger("ameba", isTrace ? TRACE : DEBUG)
root(WARN, ["CONSOLE", "FILE"])