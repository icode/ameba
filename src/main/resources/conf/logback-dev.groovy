package conf

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.*

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

logger("org.glassfish.jersey", INFO)
logger("org.glassfish.jersey.server.model.Parameter", INFO)
logger("org.glassfish.grizzly", INFO)
logger("org.avaje.ebean", INFO)
logger("httl", INFO)
logger("ameba", TRACE)
root(INFO, ["CONSOLE"])