package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import org.bukkit.Location;

import java.util.UUID;

public class LocationSoundPacket extends SoundPacket<LocationSoundPacket> {

    protected Location location;
    protected float distance;

    public LocationSoundPacket(UUID sender, Location location, byte[] data, long sequenceNumber, float distance) {
        super(sender, data, sequenceNumber);
        this.location = location;
        this.distance = distance;
    }

    public LocationSoundPacket() {

    }

    public Location getLocation() {
        return location;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public LocationSoundPacket fromBytes(FriendlyByteBuf buf) {
        LocationSoundPacket soundPacket = new LocationSoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.location = new Location(null, buf.readDouble(), buf.readDouble(), buf.readDouble());
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        soundPacket.distance = buf.readFloat();
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(sender);
        buf.writeDouble(location.getX());
        buf.writeDouble(location.getY());
        buf.writeDouble(location.getZ());
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
        buf.writeFloat(distance);
    }
}
