package de.maxhenkel.voicechat.voice.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TalkCache {

    private static final long TIMEOUT = 250L;

    private final Map<UUID, Talk> cache;

    public TalkCache() {
        this.cache = new HashMap<>();
    }

    public void updateTalking(UUID player, boolean whispering) {
        cache.put(player, new Talk(whispering));
    }

    public boolean isTalking(Player player) {
        return isTalking(player.getUUID());
    }

    public boolean isWhispering(Player player) {
        return isWhispering(player.getUUID());
    }

    public boolean isTalking(UUID player) {
        if (player.equals(Minecraft.getInstance().player.getUUID())) {
            ClientVoicechat client = ClientManager.getClient();
            if (client != null && client.getMicThread() != null) {
                return client.getMicThread().isTalking();
            }
        }

        Talk lastTalk = cache.getOrDefault(player, new Talk(0L, false));
        return System.currentTimeMillis() - lastTalk.timestamp < TIMEOUT;
    }

    public boolean isWhispering(UUID player) {
        if (player.equals(Minecraft.getInstance().player.getUUID())) {
            ClientVoicechat client = ClientManager.getClient();
            if (client != null && client.getMicThread() != null) {
                return client.getMicThread().isWhispering();
            }
        }

        Talk lastTalk = cache.getOrDefault(player, new Talk(0L, false));
        return lastTalk.whispering && System.currentTimeMillis() - lastTalk.timestamp < TIMEOUT;
    }

    private class Talk {
        private long timestamp;
        private boolean whispering;

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
