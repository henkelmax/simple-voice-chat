package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.Packet;

import javax.annotation.Nullable;

public class SoundPacketEventImpl<T extends Packet> extends PacketEventImpl<T> implements SoundPacketEvent<T> {

    private final String source;

    public SoundPacketEventImpl(VoicechatServerApi api, T packet, @Nullable VoicechatConnection senderConnection, @Nullable VoicechatConnection receiverConnection, String source) {
        super(api, packet, senderConnection, receiverConnection);
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }

}
