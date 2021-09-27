package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class TalkCache {

    private static final long TIMEOUT = 250L;

    private final Map<UUID, Long> cache;

    public TalkCache() {
        this.cache = new HashMap<>();
    }

    public void updateTalking(UUID player) {
        cache.put(player, System.currentTimeMillis());
    }

    public boolean isTalking(Player player) {
        return isTalking(player.getUUID());
    }

    public boolean isTalking(UUID player) {
        if (player.equals(Minecraft.getInstance().player.getUUID())) {
            Client client = VoicechatClient.CLIENT.getClient();
            if (client != null && client.getMicThread() != null) {
                return client.getMicThread().isTalking();
            }
        }

        Long lastTalk = cache.getOrDefault(player, 0L);
        return System.currentTimeMillis() - lastTalk < TIMEOUT;
    }

}
