package de.maxhenkel.voicechat.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaLoggingLogger implements VoiceChatLogger {

    private final boolean debugMode;
    private final Logger logger;

    public JavaLoggingLogger(Logger logger) {
        this.logger = logger;
        this.debugMode = System.getProperty("voicechat.debug") != null;
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
            return switch (level) {
                case TRACE -> Level.ALL;
                case WARN -> Level.WARNING;
                case ERROR, FATAL -> Level.SEVERE;
                default -> Level.INFO;
            };
        }
        return switch (level) {
            case TRACE -> Level.ALL;
            case DEBUG -> Level.CONFIG;
            case WARN -> Level.WARNING;
            case ERROR, FATAL -> Level.SEVERE;
            default -> Level.INFO;
        };
    }

}
