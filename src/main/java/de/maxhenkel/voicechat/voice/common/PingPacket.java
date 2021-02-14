package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketByteBuf;

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
    public PingPacket fromBytes(PacketByteBuf buf) {
        PingPacket soundPacket = new PingPacket();
        soundPacket.id = buf.readUuid();
        soundPacket.timestamp = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeUuid(id);
        buf.writeLong(timestamp);
    }
}
