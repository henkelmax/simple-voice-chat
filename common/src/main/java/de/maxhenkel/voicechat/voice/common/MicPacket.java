package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

public class MicPacket implements Packet<MicPacket> {

    private byte[] data;
    private long sequenceNumber;

    public MicPacket(byte[] data, long sequenceNumber) {
        this.data = data;
        this.sequenceNumber = sequenceNumber;
    }

    public MicPacket() {

    }

    @Override
    public long getTTL() {
        return 500L;
    }

    public byte[] getData() {
        return data;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public MicPacket fromBytes(FriendlyByteBuf buf) {
        MicPacket soundPacket = new MicPacket();
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
    }
}
