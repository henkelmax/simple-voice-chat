package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

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
    public PingPacket fromBytes(FriendlyByteBuf buf) {
        PingPacket soundPacket = new PingPacket();
        soundPacket.id = buf.readUUID();
        soundPacket.timestamp = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(id);
        buf.writeLong(timestamp);
    }
}
