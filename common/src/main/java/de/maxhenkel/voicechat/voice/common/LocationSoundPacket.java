package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.UUID;

public class LocationSoundPacket extends SoundPacket<LocationSoundPacket> {

    protected Vector3d location;
    protected float distance;

    public LocationSoundPacket(UUID channelId, UUID sender, Vector3d location, byte[] data, long sequenceNumber, float distance, @Nullable String category) {
        super(channelId, sender, data, sequenceNumber, category);
        this.location = location;
        this.distance = distance;
    }

    public LocationSoundPacket(UUID channelId, UUID sender, short[] data, Vector3d location, float distance, @Nullable String category) {
        super(channelId, sender, data, category);
        this.location = location;
        this.distance = distance;
    }

    public LocationSoundPacket() {

    }

    public Vector3d getLocation() {
        return location;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public LocationSoundPacket fromBytes(PacketBuffer buf) {
        LocationSoundPacket soundPacket = new LocationSoundPacket();
        soundPacket.channelId = buf.readUUID();
        soundPacket.sender = buf.readUUID();
        soundPacket.location = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        soundPacket.distance = buf.readFloat();

        byte data = buf.readByte();
        if (hasFlag(data, HAS_CATEGORY_MASK)) {
            soundPacket.category = buf.readUtf(16);
        }

        return soundPacket;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(channelId);
        buf.writeUUID(sender);
        buf.writeDouble(location.x);
        buf.writeDouble(location.y);
        buf.writeDouble(location.z);
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
        buf.writeFloat(distance);

        byte data = 0b0;
        if (category != null) {
            data = setFlag(data, HAS_CATEGORY_MASK);
        }
        buf.writeByte(data);
        if (category != null) {
            buf.writeUtf(category, 16);
        }
    }
}
