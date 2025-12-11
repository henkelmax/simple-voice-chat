package de.maxhenkel.voicechat.debug;

import java.util.concurrent.ConcurrentHashMap;

public class CooldownTimer {

    private static ConcurrentHashMap<String, Long> cooldowns;

    static {
        cooldowns = new ConcurrentHashMap<>();
    }

    public static void run(String id, long time, Runnable runnable) {
        if (System.currentTimeMillis() - cooldowns.getOrDefault(id, 0L) > time) {
            cooldowns.put(id, System.currentTimeMillis());
            runnable.run();
        }
    }

    public static void run(String id, Runnable runnable) {
        run(id, 10_000L, runnable);
    }

}
