package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

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
    public PingPacket fromBytes(ByteBuf buf) {
        PingPacket soundPacket = new PingPacket();
        soundPacket.id = BufferUtils.readUUID(buf);
        soundPacket.timestamp = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BufferUtils.writeUUID(buf, id);
        buf.writeLong(timestamp);
    }
}
