package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;

public class MicrophonePacketEventImpl extends PacketEventImpl<MicrophonePacket> implements MicrophonePacketEvent {

    public MicrophonePacketEventImpl(MicrophonePacket packet, VoicechatConnection connection) {
        super(packet, connection, null);
    }
}
