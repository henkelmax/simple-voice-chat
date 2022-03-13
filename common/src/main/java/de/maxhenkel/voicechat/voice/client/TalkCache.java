package de.maxhenkel.voicechat.voice.client;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class TalkCache {

    private static final long TIMEOUT = 250L;

    private final Map<UUID, Talk> cache;
    private Supplier<Long> timestampSupplier;

    public TalkCache() {
        this.cache = new HashMap<>();
        this.timestampSupplier = System::currentTimeMillis;
    }

    public void setTimestampSupplier(Supplier<Long> timestampSupplier) {
        this.timestampSupplier = timestampSupplier;
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
                if (client.getMicThread().isTalking()) {
                    return true;
                }
            }
        }

        Talk lastTalk = cache.getOrDefault(entity, new Talk(0L, false));
        return timestampSupplier.get() - lastTalk.timestamp < TIMEOUT;
    }

    public boolean isWhispering(UUID entity) {
        if (entity.equals(ClientManager.getPlayerStateManager().getOwnID())) {
            ClientVoicechat client = ClientManager.getClient();
            if (client != null && client.getMicThread() != null) {
                if (client.getMicThread().isWhispering()) {
                    return true;
                }
            }
        }

        Talk lastTalk = cache.getOrDefault(entity, new Talk(0L, false));
        return lastTalk.whispering && timestampSupplier.get() - lastTalk.timestamp < TIMEOUT;
    }

    private class Talk {
        private final long timestamp;
        private final boolean whispering;

        public Talk(long timestamp, boolean whispering) {
            this.timestamp = timestamp;
            this.whispering = whispering;
        }

        public Talk(boolean whispering) {
            this.timestamp = timestampSupplier.get();
            this.whispering = whispering;
        }
    }

}
