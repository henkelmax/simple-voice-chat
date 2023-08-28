package de.maxhenkel.voicechat.logging;

import de.maxhenkel.voicechat.Voicechat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.util.StackLocatorUtil;

import java.util.Map;

public class Log4JVoicechatLogger implements VoicechatLogger {

    private final boolean debugMode;
    private final Logger logger;

    public Log4JVoicechatLogger(Logger logger) {
        this.logger = logger;
        this.debugMode = Voicechat.debugMode();

        try {
            if (debugMode) {
                initDebugLogLevel();
            }
        } catch (Throwable t) {
            logger.error("Failed to set log level", t);
        }
    }

    public Log4JVoicechatLogger(String name) {
        this(LogManager.getLogger(name));
    }

    private void initDebugLogLevel() throws Exception {
        if (!(logger instanceof org.apache.logging.log4j.core.Logger)) {
            throw new IllegalStateException("Logger is not an instance of org.apache.logging.log4j.core.Logger");
        }
        org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) logger;
        Map<String, Appender> appenders = coreLogger.getAppenders();
        coreLogger.setAdditive(false);
        Configurator.setLevel(logger, Level.DEBUG);
        for (Appender appender : appenders.values()) {
            coreLogger.addAppender(appender);
        }
    }

    @Override
    public void log(LogLevel level, String message, Object... args) {
        if (!isEnabled(level)) {
            return;
        }
        logger.log(fromLogLevel(level), modifyMessage(message), args);
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return logger.isEnabled(fromLogLevel(level));
    }

    private Level fromLogLevel(LogLevel level) {
        switch (level) {
            case TRACE:
                return Level.TRACE;
            case DEBUG:
                return Level.DEBUG;
            case WARN:
                return Level.WARN;
            case ERROR:
                return Level.ERROR;
            case FATAL:
                return Level.FATAL;
            default:
                return Level.INFO;
        }
    }

    private String modifyMessage(String message) {
        if (debugMode) {
            return String.format("[%s/%s] %s", logger.getName(), StackLocatorUtil.getCallerClass(4).getSimpleName(), message);
        }
        return String.format("[%s] %s", logger.getName(), message);
    }

    public Logger getLogger() {
        return logger;
    }

}
