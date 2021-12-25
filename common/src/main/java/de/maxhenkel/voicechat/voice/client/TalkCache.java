package de.maxhenkel.voicechat.voice.client;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TalkCache {

    private static final long TIMEOUT = 250L;

    private final Map<UUID, Talk> cache;

    public TalkCache() {
        this.cache = new HashMap<>();
    }

    public void updateTalking(UUID entity, boolean whispering) {
        cache.put(entity, new Talk(whispering));
    }

    public boolean isTalking(Entity entity) {
        return isTalking(entity.getUUID());
    }

    public boolean isWhispering(Entity entity) {
        return isWhispering(entity.getUUID());
    }

    public boolean isTalking(UUID entity) {
        if (entity.equals(ClientManager.getPlayerStateManager().getOwnID())) {
            ClientVoicechat client = ClientManager.getClient();
            if (client != null && client.getMicThread() != null) {
                return client.getMicThread().isTalking();
            }
        }

        Talk lastTalk = cache.getOrDefault(entity, new Talk(0L, false));
        return System.currentTimeMillis() - lastTalk.timestamp < TIMEOUT;
    }

    public boolean isWhispering(UUID entity) {
        if (entity.equals(ClientManager.getPlayerStateManager().getOwnID())) {
            ClientVoicechat client = ClientManager.getClient();
            if (client != null && client.getMicThread() != null) {
                return client.getMicThread().isWhispering();
            }
        }

        Talk lastTalk = cache.getOrDefault(entity, new Talk(0L, false));
        return lastTalk.whispering && System.currentTimeMillis() - lastTalk.timestamp < TIMEOUT;
    }

    private static class Talk {
        private final long timestamp;
        private final boolean whispering;

        public Talk(long timestamp, boolean whispering) {
            this.timestamp = timestamp;
            this.whispering = whispering;
        }

        public Talk(boolean whispering) {
            this.timestamp = System.currentTimeMillis();
            this.whispering = whispering;
        }
    }

}
