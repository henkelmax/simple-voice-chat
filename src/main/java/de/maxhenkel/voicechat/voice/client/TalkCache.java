package de.maxhenkel.voicechat.voice.client;

import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public class TalkCache {

    private static final long TIMEOUT = 500L;

    private final Map<UUID, Long> cache;

    public TalkCache() {
        this.cache = new HashMap<>();
    }

    public void updateTalking(UUID player) {
        cache.put(player, System.currentTimeMillis());
    }

    public void updateCache() {
        long time = System.currentTimeMillis();
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : cache.entrySet()) {
            if (time - entry.getValue() > TIMEOUT) {
                toRemove.add(entry.getKey());
            }
        }
        for (UUID uuid : toRemove) {
            cache.remove(uuid);
        }
    }

    public boolean isTalking(PlayerEntity playerEntity) {
        updateCache();
        return cache.containsKey(playerEntity.getUUID());
    }

}
