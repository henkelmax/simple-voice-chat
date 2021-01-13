package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

import java.util.UUID;

public class PingPacket implements Packet<PingPacket> {

    private UUID id;
    private long timestamp;

    public PingPacket(UUID id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public PingPacket() {

    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public PingPacket fromBytes(PacketBuffer buf) {
        PingPacket soundPacket = new PingPacket();
        soundPacket.id = buf.readUniqueId();
        soundPacket.timestamp = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(id);
        buf.writeLong(timestamp);
    }
}