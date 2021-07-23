package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class SoundPacket implements Packet<SoundPacket> {

    private UUID sender;
    private byte[] data;
    private long sequenceNumber;

    public SoundPacket(UUID sender, byte[] data, long sequenceNumber) {
        this.sender = sender;
        this.data = data;
        this.sequenceNumber = sequenceNumber;
    }

    public SoundPacket() {

    }

    public byte[] getData() {
        return data;
    }

    public UUID getSender() {
        return sender;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public SoundPacket fromBytes(FriendlyByteBuf buf) {
        SoundPacket soundPacket = new SoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(sender);
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
    }
}
