package conf

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.*

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

logger("org.glassfish.jersey", TRACE)
logger("org.glassfish.jersey.server.model.Parameter", INFO)
logger("org.glassfish.grizzly", TRACE)
logger("org.avaje.ebean", TRACE)
logger("httl", TRACE)
logger("ameba", TRACE)
root(DEBUG, ["CONSOLE"])