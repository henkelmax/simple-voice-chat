package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;

import javax.annotation.Nullable;

public class EntitySoundPacketEventImpl extends SoundPacketEventImpl<EntitySoundPacket> implements EntitySoundPacketEvent {

    public EntitySoundPacketEventImpl(EntitySoundPacket packet, @Nullable VoicechatConnection senderConnection, VoicechatConnection receiverConnection, String source) {
        super(packet, senderConnection, receiverConnection, source);
    }
}
