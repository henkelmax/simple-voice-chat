package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;

import javax.annotation.Nullable;

public class EntitySoundPacketEventImpl extends PacketEventImpl<EntitySoundPacket> implements EntitySoundPacketEvent {

    public EntitySoundPacketEventImpl(VoicechatServerApi api, EntitySoundPacket packet, @Nullable VoicechatConnection senderConnection, VoicechatConnection receiverConnection) {
        super(api, packet, senderConnection, receiverConnection);
    }
}
