package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.StaticSoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;

import javax.annotation.Nullable;

public class StaticSoundPacketEventImpl extends SoundPacketEventImpl<StaticSoundPacket> implements StaticSoundPacketEvent {

    public StaticSoundPacketEventImpl(VoicechatServerApi api, StaticSoundPacket packet, @Nullable VoicechatConnection senderConnection, VoicechatConnection receiverConnection, String source) {
        super(api, packet, senderConnection, receiverConnection, source);
    }
}
