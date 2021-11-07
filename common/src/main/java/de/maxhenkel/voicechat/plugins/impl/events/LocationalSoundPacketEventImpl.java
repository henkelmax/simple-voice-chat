package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;

import javax.annotation.Nullable;

public class LocationalSoundPacketEventImpl extends PacketEventImpl<LocationalSoundPacket> implements LocationalSoundPacketEvent {

    public LocationalSoundPacketEventImpl(VoicechatServerApi api, LocationalSoundPacket packet, @Nullable VoicechatConnection senderConnection, VoicechatConnection receiverConnection) {
        super(api, packet, senderConnection, receiverConnection);
    }
}
