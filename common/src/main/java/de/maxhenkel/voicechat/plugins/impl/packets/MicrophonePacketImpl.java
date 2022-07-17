package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;
import de.maxhenkel.voicechat.voice.common.*;

import java.util.UUID;

public class MicrophonePacketImpl implements MicrophonePacket {

    private final MicPacket packet;
    private final UUID sender;

    public MicrophonePacketImpl(MicPacket packet, UUID sender) {
        this.packet = packet;
        this.sender = sender;
    }

    @Override
    public boolean isWhispering() {
        return packet.isWhispering();
    }

    @Override
    public byte[] getOpusEncodedData() {
        return packet.getData();
    }

    @Override
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering) {
        return toEntitySoundPacket(entityUuid, whispering, Utils.getDefaultDistance());
    }

    @Override
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering, float distance) {
        return new EntitySoundPacketImpl(new PlayerSoundPacket(sender, packet.getData(), packet.getSequenceNumber(), whispering, distance));
    }

    @Override
    public LocationalSoundPacket toLocationalSoundPacket(Position position) {
        return toLocationalSoundPacket(position, Utils.getDefaultDistance());
    }

    @Override
    public LocationalSoundPacket toLocationalSoundPacket(Position position, float distance) {
        if (position instanceof PositionImpl) {
            PositionImpl p = (PositionImpl) position;
            return new LocationalSoundPacketImpl(new LocationSoundPacket(sender, p.getPosition(), packet.getData(), packet.getSequenceNumber(), distance));
        } else {
            throw new IllegalArgumentException("position is not an instance of PositionImpl");
        }
    }

    @Override
    public StaticSoundPacket toStaticSoundPacket() {
        return new StaticSoundPacketImpl(new GroupSoundPacket(sender, packet.getData(), packet.getSequenceNumber()));
    }

}
