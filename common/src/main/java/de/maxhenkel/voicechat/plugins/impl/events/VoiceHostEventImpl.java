package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.VoiceHostEvent;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import net.minecraft.entity.player.EntityPlayerMP;

public class VoiceHostEventImpl extends ServerEventImpl implements VoiceHostEvent {

    private final ServerPlayerImpl player;
    private String voiceHost;

    public VoiceHostEventImpl(EntityPlayerMP player, String voiceHost) {
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
    public de.maxhenkel.voicechat.api.ServerPlayer getPlayer() {
        return player;
    }

}
