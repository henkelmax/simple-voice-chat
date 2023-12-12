package de.maxhenkel.voicechat.voice.client;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class TalkCache {

    private static final long TIMEOUT = 250L;

    private static final Talk DEFAULT = new Talk(0L, false);

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
        Talk talk = cache.get(entity);
        if (talk == null) {
            talk = new Talk(timestampSupplier.get(), whispering);
            cache.put(entity, talk);
        } else {
            talk.timestamp = timestampSupplier.get();
            talk.whispering = whispering;
        }
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

        Talk lastTalk = cache.getOrDefault(entity, DEFAULT);
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

        Talk lastTalk = cache.getOrDefault(entity, DEFAULT);
        return lastTalk.whispering && timestampSupplier.get() - lastTalk.timestamp < TIMEOUT;
    }

    private static class Talk {
        private long timestamp;
        private boolean whispering;

        public Talk(long timestamp, boolean whispering) {
            this.timestamp = timestamp;
            this.whispering = whispering;
        }
    }

}
