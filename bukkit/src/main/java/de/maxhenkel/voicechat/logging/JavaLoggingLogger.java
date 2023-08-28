package de.maxhenkel.voicechat.logging;

import de.maxhenkel.voicechat.Voicechat;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaLoggingLogger implements VoicechatLogger {

    private final boolean debugMode;
    private final Logger logger;

    public JavaLoggingLogger(Logger logger) {
        this.logger = logger;
        this.debugMode = Voicechat.debugMode();
    }

    @Override
    public void log(LogLevel level, String message, Object... args) {
        if (!isEnabled(level)) {
            return;
        }
        Throwable throwable = null;
        if (args.length > 0) {
            if (args[args.length - 1] instanceof Throwable) {
                throwable = (Throwable) args[args.length - 1];
                Object[] newArgs = new Object[args.length - 1];
                System.arraycopy(args, 0, newArgs, 0, newArgs.length);
                args = newArgs;
            }
        }
        logger.log(fromLogLevel(level), replacePlaceholders(message, args), throwable);
    }

    private static final String PLACEHOLDER = "{}";

    private String replacePlaceholders(String message, Object... args) {
        StringBuilder formattedMessage = new StringBuilder();
        int argIndex = 0;
        int placeholderStart = message.indexOf(PLACEHOLDER);

        while (placeholderStart >= 0 && argIndex < args.length) {
            formattedMessage.append(message, 0, placeholderStart);
            formattedMessage.append(args[argIndex]);
            message = message.substring(placeholderStart + PLACEHOLDER.length());
            argIndex++;
            placeholderStart = message.indexOf(PLACEHOLDER);
        }

        formattedMessage.append(message);
        return formattedMessage.toString();
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return logger.isLoggable(fromLogLevel(level));
    }

    private Level fromLogLevel(LogLevel level) {
        if (debugMode) {
            switch (level) {
                case TRACE:
                    return Level.ALL;
                case WARN:
                    return Level.WARNING;
                case ERROR:
                case FATAL:
                    return Level.SEVERE;
                default:
                    return Level.INFO;
            }
        }
        switch (level) {
            case TRACE:
                return Level.ALL;
            case DEBUG:
                return Level.CONFIG;
            case WARN:
                return Level.WARNING;
            case ERROR:
            case FATAL:
                return Level.SEVERE;
            default:
                return Level.INFO;
        }
    }

    public Logger getLogger() {
        return logger;
    }

}
