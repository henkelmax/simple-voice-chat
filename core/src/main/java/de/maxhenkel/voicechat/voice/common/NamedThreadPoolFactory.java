package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;

import java.util.concurrent.ThreadFactory;

public class NamedThreadPoolFactory implements ThreadFactory {

    private final String name;

    public NamedThreadPoolFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, name);
        thread.setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());
        thread.setDaemon(true);
        return thread;
    }

    public static NamedThreadPoolFactory create(String name) {
        return new NamedThreadPoolFactory(name);
    }

}
