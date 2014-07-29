package conf

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.*

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

logger("org.glassfish.jersey.filter.LoggingFilter", INFO)
logger("org.glassfish", WARN)
logger("org.avaje.ebean", WARN)
logger("httl", WARN)
logger("ameba", TRACE)
root(WARN, ["CONSOLE"])