package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

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
        return 2_000L;
    }

    public byte[] getData() {
        return data;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public MicPacket fromBytes(PacketBuffer buf) {
        MicPacket soundPacket = new MicPacket();
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
    }
}
