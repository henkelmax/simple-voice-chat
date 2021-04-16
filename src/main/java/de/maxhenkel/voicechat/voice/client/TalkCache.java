package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.client.MinecraftClient;
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

    public boolean isTalking(PlayerEntity player) {
        return isTalking(player.getUuid());
    }

    public boolean isTalking(UUID player) {
        if (player.equals(MinecraftClient.getInstance().player.getUuid())) {
            Client client = VoicechatClient.CLIENT.getClient();
            if (client != null) {
                return client.getMicThread().isTalking();
            }
        }
        updateCache();
        return cache.containsKey(player);
    }

}
