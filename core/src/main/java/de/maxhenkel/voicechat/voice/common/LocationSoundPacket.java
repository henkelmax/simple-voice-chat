package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.UUID;

public class LocationSoundPacket extends SoundPacket<LocationSoundPacket> {

    protected Position location;
    protected float distance;

    public LocationSoundPacket(UUID channelId, UUID sender, Position location, byte[] data, long sequenceNumber, float distance, @Nullable String category) {
        super(channelId, sender, data, sequenceNumber, category);
        this.location = location;
        this.distance = distance;
    }

    public LocationSoundPacket(UUID channelId, UUID sender, short[] data, Position location, float distance, @Nullable String category) {
        super(channelId, sender, data, category);
        this.location = location;
        this.distance = distance;
    }

    public LocationSoundPacket() {

    }

    public Position getLocation() {
        return location;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public LocationSoundPacket fromBytes(ByteBuf buf) {
        LocationSoundPacket soundPacket = new LocationSoundPacket();
        soundPacket.channelId = BufferUtils.readUUID(buf);
        soundPacket.sender = BufferUtils.readUUID(buf);
        soundPacket.location = CommonCompatibilityManager.INSTANCE.createPosition(buf.readDouble(), buf.readDouble(), buf.readDouble());
        soundPacket.data = BufferUtils.readByteArray(buf);
        soundPacket.sequenceNumber = buf.readLong();
        soundPacket.distance = buf.readFloat();

        byte data = buf.readByte();
        if (hasFlag(data, HAS_CATEGORY_MASK)) {
            soundPacket.category = BufferUtils.readUtf(buf, 16);
        }

        return soundPacket;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BufferUtils.writeUUID(buf, channelId);
        BufferUtils.writeUUID(buf, sender);
        buf.writeDouble(location.getX());
        buf.writeDouble(location.getY());
        buf.writeDouble(location.getZ());
        BufferUtils.writeByteArray(buf, data);
        buf.writeLong(sequenceNumber);
        buf.writeFloat(distance);

        byte data = 0b0;
        if (category != null) {
            data = setFlag(data, HAS_CATEGORY_MASK);
        }
        buf.writeByte(data);
        if (category != null) {
            BufferUtils.writeUtf(buf, category, 16);
        }
    }
}
