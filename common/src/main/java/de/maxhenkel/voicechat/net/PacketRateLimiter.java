package de.maxhenkel.voicechat.net;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnstableApiUsage")
public class PacketRateLimiter {

    private final ConcurrentHashMap<UUID, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private final int maxPacketsPerSecond;

    public PacketRateLimiter(int maxPacketsPerSecond) {
        this.maxPacketsPerSecond = maxPacketsPerSecond;
    }

    public boolean allow(UUID player) {
        if (maxPacketsPerSecond <= 0) {
            return true;
        }
        RateLimiter limiter = rateLimiters.computeIfAbsent(player, id -> RateLimiter.create(maxPacketsPerSecond));
        return limiter.tryAcquire();
    }

    public void onPlayerLoggedOut(EntityPlayerMP player) {
        rateLimiters.remove(player.getUniqueID());
    }
}
