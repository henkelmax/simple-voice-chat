package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class TalkCache {

    private static final long TIMEOUT = 250L;

    private static final PlayerCache DEFAULT = new PlayerCache(0L, false, AudioUtils.LOWEST_DB);

    private final Map<UUID, PlayerCache> playerCache;
    private final Map<String, CategoryCache> categoryCache;
    private Supplier<Long> timestampSupplier;

    public TalkCache() {
        this.playerCache = new HashMap<>();
        this.categoryCache = new HashMap<>();
        this.timestampSupplier = System::currentTimeMillis;
    }

    public void setTimestampSupplier(Supplier<Long> timestampSupplier) {
        this.timestampSupplier = timestampSupplier;
    }

    private void updateTalking(UUID entity, boolean whispering, double audioLevel) {
        PlayerCache talk = playerCache.get(entity);
        if (talk == null) {
            talk = new PlayerCache(timestampSupplier.get(), whispering, audioLevel);
            playerCache.put(entity, talk);
        } else {
            talk.timestamp = timestampSupplier.get();
            talk.whispering = whispering;
            talk.audioLevel = audioLevel;
        }
    }

    /**
     * This just exists for backwards compatibility with flashback
     *
     * @param entity     the entity UUID
     * @param whispering if the entity is whispering
     * @deprecated Use {@link #updateLevel(UUID, String, boolean, short[])} instead
     */
    @Deprecated
    public void updateTalking(UUID entity, boolean whispering) {
        updateTalking(entity, whispering, AudioUtils.LOWEST_DB);
    }

    /**
     * Updates the audio level of a player talking or a specific category
     *
     * @param id         the entity UUID
     * @param category   the category name or null if it is a player
     * @param whispering if the entity is whispering
     * @param audio      the audio data to calculate the audio level from
     */
    public void updateLevel(UUID id, @Nullable String category, boolean whispering, short[] audio) {
        double highestAudioLevel = AudioUtils.getHighestAudioLevel(audio);
        if (category != null) {
            updateCategoryVolume(category, highestAudioLevel);
        }
        // Update the player talking even if it is a category
        updateTalking(id, whispering, highestAudioLevel);
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

        PlayerCache lastTalk = playerCache.getOrDefault(entity, DEFAULT);
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

        PlayerCache lastTalk = playerCache.getOrDefault(entity, DEFAULT);
        return lastTalk.whispering && timestampSupplier.get() - lastTalk.timestamp < TIMEOUT;
    }

    public void updateCategoryVolume(String category, double audioLevel) {
        CategoryCache cache = categoryCache.get(category);
        if (cache == null) {
            cache = new CategoryCache(timestampSupplier.get(), audioLevel);
            categoryCache.put(category, cache);
        } else {
            cache.timestamp = timestampSupplier.get();
            cache.audioLevel = audioLevel;
        }
    }

    public double getPlayerAudioLevel(UUID entity) {
        PlayerCache talk = playerCache.getOrDefault(entity, DEFAULT);
        if (timestampSupplier.get() - talk.timestamp >= TIMEOUT) {
            return AudioUtils.LOWEST_DB;
        }
        return talk.audioLevel;
    }

    public double getCategoryAudioLevel(String category) {
        CategoryCache cache = categoryCache.get(category);
        if (cache == null) {
            return AudioUtils.LOWEST_DB;
        }
        if (timestampSupplier.get() - cache.timestamp >= TIMEOUT) {
            return AudioUtils.LOWEST_DB;
        }
        return cache.audioLevel;
    }

    private static class PlayerCache {
        private long timestamp;
        private boolean whispering;
        private double audioLevel;

        public PlayerCache(long timestamp, boolean whispering, double audioLevel) {
            this.timestamp = timestamp;
            this.whispering = whispering;
            this.audioLevel = audioLevel;
        }
    }

    private static class CategoryCache {
        private long timestamp;
        private double audioLevel;

        public CategoryCache(long timestamp, double audioLevel) {
            this.timestamp = timestamp;
            this.audioLevel = audioLevel;
        }
    }

}
