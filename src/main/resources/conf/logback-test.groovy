package conf

import ameba.util.IOUtils
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

import static ch.qos.logback.classic.Level.*

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

String appName = context.getProperty("appName");
appender("FILE", RollingFileAppender) {
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = IOUtils.getResource("").getFile() + "../logs/" + appName + ".%d{yyyy-MM-dd}-%i.log"
        maxHistory = 30
        timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
            maxFileSize = "20MB"
        }
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

String appPackage = context.getProperty("appPackage");
if (appPackage != null)
    logger(appPackage, DEBUG)

logger("org.glassfish.jersey.filter.LoggingFilter", INFO)
logger("org.glassfish", WARN)
logger("org.avaje.ebean", WARN)
logger("httl", WARN)
logger("ameba", TRACE)
root(WARN, ["CONSOLE", "FILE"])