package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.events.VoiceHostEvent;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import org.bukkit.entity.Player;

public class VoiceHostEventImpl extends ServerEventImpl implements VoiceHostEvent {

    private final ServerPlayerImpl player;
    private String voiceHost;

    public VoiceHostEventImpl(Player player, String voiceHost) {
        this.player = new ServerPlayerImpl(player);
        this.voiceHost = voiceHost;
    }

    @Override
    public String getVoiceHost() {
        return voiceHost;
    }

    @Override
    public void setVoiceHost(String voiceHost) {
        this.voiceHost = voiceHost;
    }

    @Override
    public ServerPlayer getPlayer() {
        return player;
    }

}
