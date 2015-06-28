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

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}
Properties properties = context.getObject("properties");
String appName = properties.getProperty("app.name");
if (StringUtils.isBlank(appName)) {
    appName = Application.DEFAULT_APP_NAME;
}
String appPackage = properties.getProperty("app.package");

appender("FILE", RollingFileAppender) {
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = IOUtils.getResource("").getFile() + "logs/" + appName + ".%d{yyyy-MM-dd}-%i.log"
        maxHistory = 30
        timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
            maxFileSize = "20MB"
        }
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

if (appPackage != null)
    logger(appPackage, INFO)
logger("ameba", INFO)
logger("org.glassfish.jersey.message.internal", OFF)
logger("org.glassfish.jersey.server.ServerRuntime\$Responder", OFF)
root(WARN, ["CONSOLE", "FILE"])