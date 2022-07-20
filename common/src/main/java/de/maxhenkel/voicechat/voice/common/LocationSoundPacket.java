package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class LocationSoundPacket extends SoundPacket<LocationSoundPacket> {

    protected Vec3 location;
    protected float distance;

    public LocationSoundPacket(UUID sender, Vec3 location, byte[] data, long sequenceNumber, float distance, @Nullable String category) {
        super(sender, data, sequenceNumber, category);
        this.location = location;
        this.distance = distance;
    }

    public LocationSoundPacket(UUID sender, short[] data, Vec3 location, float distance, @Nullable String category) {
        super(sender, data, category);
        this.location = location;
        this.distance = distance;
    }

    public LocationSoundPacket() {

    }

    public Vec3 getLocation() {
        return location;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public LocationSoundPacket fromBytes(FriendlyByteBuf buf) {
        LocationSoundPacket soundPacket = new LocationSoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.location = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
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
    public void toBytes(FriendlyByteBuf buf) {
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
