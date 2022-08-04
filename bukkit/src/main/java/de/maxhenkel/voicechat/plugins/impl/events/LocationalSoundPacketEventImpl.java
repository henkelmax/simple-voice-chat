package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;

import javax.annotation.Nullable;

public class LocationalSoundPacketEventImpl extends SoundPacketEventImpl<LocationalSoundPacket> implements LocationalSoundPacketEvent {

    public LocationalSoundPacketEventImpl(LocationalSoundPacket packet, @Nullable VoicechatConnection senderConnection, VoicechatConnection receiverConnection, String source) {
        super(packet, senderConnection, receiverConnection, source);
    }
}
