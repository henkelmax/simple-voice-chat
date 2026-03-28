package de.maxhenkel.voicechat.net;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketRateLimiter {

    private final ConcurrentHashMap<UUID, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private final int maxPacketsPerSecond;
    private final int timeWindowSeconds = 5;

    public PacketRateLimiter(int maxPacketsPerSecond) {
        this.maxPacketsPerSecond = maxPacketsPerSecond;
    }

    public boolean allow(UUID player) {
        if (maxPacketsPerSecond <= 0) {
            return true;
        }
        RateLimiter limiter = rateLimiters.computeIfAbsent(player, id -> new RateLimiter(maxPacketsPerSecond * timeWindowSeconds, 1000 * timeWindowSeconds));
        return limiter.tryAcquire();
    }

    public void onPlayerLoggedOut(Player player) {
        rateLimiters.remove(player.getUniqueId());
    }

    private static class RateLimiter {
        private final int threshold;
        private final long timePerTokenNanos;
        private long lastLeakNanos;
        private long amount;

        public RateLimiter(int threshold, long windowMillis) {
            this.threshold = threshold;
            this.timePerTokenNanos = (windowMillis * 1_000_000L) / threshold;
            this.lastLeakNanos = System.nanoTime();
            this.amount = 0L;
        }

        public boolean tryAcquire() {
            long currentTime = System.nanoTime();
            long elapsed = currentTime - lastLeakNanos;
            long leakedTokens = elapsed / timePerTokenNanos;

            if (leakedTokens > 0) {
                amount -= leakedTokens;
                if (amount < 0) {
                    amount = 0;
                }
                lastLeakNanos += leakedTokens * timePerTokenNanos;
            }

            if (amount >= threshold) {
                return false;
            }

            amount++;
            return true;
        }
    }

}
