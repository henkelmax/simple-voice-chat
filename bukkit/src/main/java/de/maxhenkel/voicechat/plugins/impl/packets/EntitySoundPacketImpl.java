package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;

import java.util.UUID;

public class EntitySoundPacketImpl extends SoundPacketImpl implements EntitySoundPacket {

    private final PlayerSoundPacket packet;

    public EntitySoundPacketImpl(PlayerSoundPacket packet) {
        super(packet);
        this.packet = packet;
    }

    @Override
    public UUID getEntityUuid() {
        return packet.getSender();
    }

    @Override
    public boolean isWhispering() {
        return packet.isWhispering();
    }

    @Override
    public PlayerSoundPacket getPacket() {
        return packet;
    }
}
