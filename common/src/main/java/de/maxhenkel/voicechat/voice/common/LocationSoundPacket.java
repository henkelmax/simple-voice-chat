package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class LocationSoundPacket extends SoundPacket<LocationSoundPacket> {

    protected Vec3 location;

    public LocationSoundPacket(UUID sender, Vec3 location, byte[] data, long sequenceNumber) {
        super(sender, data, sequenceNumber);
        this.location = location;
    }

    public LocationSoundPacket() {

    }

    public Vec3 getLocation() {
        return location;
    }

    @Override
    public LocationSoundPacket fromBytes(FriendlyByteBuf buf) {
        LocationSoundPacket soundPacket = new LocationSoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.location = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
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
    }
}
