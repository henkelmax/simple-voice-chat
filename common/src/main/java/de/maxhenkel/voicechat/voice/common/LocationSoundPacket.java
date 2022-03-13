package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

import java.util.UUID;

public class LocationSoundPacket extends SoundPacket<LocationSoundPacket> {

    protected Vector3d location;

    public LocationSoundPacket(UUID sender, Vector3d location, byte[] data, long sequenceNumber) {
        super(sender, data, sequenceNumber);
        this.location = location;
    }

    public LocationSoundPacket(UUID sender, short[] data, Vec3 location) {
        super(sender, data);
        this.location = location;
    }

    public LocationSoundPacket() {

    }

    public Vector3d getLocation() {
        return location;
    }

    @Override
    public LocationSoundPacket fromBytes(PacketBuffer buf) {
        LocationSoundPacket soundPacket = new LocationSoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.location = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(sender);
        buf.writeDouble(location.x);
        buf.writeDouble(location.y);
        buf.writeDouble(location.z);
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
    }
}
