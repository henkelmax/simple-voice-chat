package de.maxhenkel.voicechat.logging;

public interface VoicechatLogger {

    void log(LogLevel level, String message, Object... args);

    boolean isEnabled(LogLevel level);

    default void trace(String message, Object... args){
        log(LogLevel.TRACE, message, args);
    }

    default void debug(String message, Object... args){
        log(LogLevel.DEBUG, message, args);
    }

    default void info(String message, Object... args){
        log(LogLevel.INFO, message, args);
    }

    default void warn(String message, Object... args){
        log(LogLevel.WARN, message, args);
    }

    default void error(String message, Object... args){
        log(LogLevel.ERROR, message, args);
    }

    default void fatal(String message, Object... args){
        log(LogLevel.FATAL, message, args);
    }

}
