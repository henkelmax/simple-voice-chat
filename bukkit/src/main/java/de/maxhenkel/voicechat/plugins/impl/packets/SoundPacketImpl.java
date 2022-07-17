package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.SoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;
import de.maxhenkel.voicechat.voice.common.GroupSoundPacket;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import java.util.UUID;

public class SoundPacketImpl implements SoundPacket {

    private final de.maxhenkel.voicechat.voice.common.SoundPacket<?> packet;

    public SoundPacketImpl(de.maxhenkel.voicechat.voice.common.SoundPacket<?> packet) {
        this.packet = packet;
    }

    @Override
    public UUID getSender() {
        return packet.getSender();
    }

    @Override
    public byte[] getOpusEncodedData() {
        return packet.getData();
    }

    @Override
    public long getSequenceNumber() {
        return packet.getSequenceNumber();
    }

    public de.maxhenkel.voicechat.voice.common.SoundPacket<?> getPacket() {
        return packet;
    }

    @Override
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering) {
        return toEntitySoundPacket(entityUuid, whispering, getDistance());
    }

    @Override
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering, float distance) {
        return new EntitySoundPacketImpl(new PlayerSoundPacket(packet.getSender(), packet.getData(), packet.getSequenceNumber(), whispering, distance));
    }

    @Override
    public LocationalSoundPacket toLocationalSoundPacket(Position position) {
        return toLocationalSoundPacket(position, getDistance());
    }

    @Override
    public LocationalSoundPacket toLocationalSoundPacket(Position position, float distance) {
        if (position instanceof PositionImpl) {
            return new LocationalSoundPacketImpl(new LocationSoundPacket(packet.getSender(), ((PositionImpl) position).getPosition(), packet.getData(), packet.getSequenceNumber(), distance));
        } else {
            throw new IllegalArgumentException("position is not an instance of PositionImpl");
        }
    }

    private float getDistance() {
        if (this instanceof EntitySoundPacket p) {
            return p.getDistance();
        } else if (this instanceof LocationalSoundPacket p) {
            return p.getDistance();
        }
        return Utils.getDefaultDistance();
    }

    @Override
    public StaticSoundPacket toStaticSoundPacket() {
        return new StaticSoundPacketImpl(new GroupSoundPacket(packet.getSender(), packet.getData(), packet.getSequenceNumber()));
    }
}
