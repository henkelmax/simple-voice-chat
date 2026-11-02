package de.maxhenkel.voicechat.debug;

import java.util.concurrent.ConcurrentHashMap;

public class CooldownTimer {

    private static final long CLEANUP_INTERVAL = 60_000L;

    private static ConcurrentHashMap<String, Long> cooldowns;
    private static volatile long lastCleanup;

    static {
        cooldowns = new ConcurrentHashMap<>();
    }

    public static void run(String id, long time, Runnable runnable) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > cooldowns.getOrDefault(id, 0L)) {
            cooldowns.put(id, currentTime + time);
            runnable.run();
        }
        cleanup(currentTime);
    }

    public static void run(String id, Runnable runnable) {
        run(id, 10_000L, runnable);
    }

    private static void cleanup(long currentTime) {
        if (currentTime - lastCleanup < CLEANUP_INTERVAL) {
            return;
        }
        lastCleanup = currentTime;
        cooldowns.values().removeIf(cooldownEnd -> cooldownEnd < currentTime);
    }

}
