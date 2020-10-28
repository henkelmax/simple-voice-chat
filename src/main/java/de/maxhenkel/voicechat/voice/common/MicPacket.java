package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

public class MicPacket implements Packet<MicPacket> {

    private byte[] data;

    public MicPacket(byte[] data) {
        this.data = data;
    }

    public MicPacket() {

    }

    public byte[] getData() {
        return data;
    }

    @Override
    public MicPacket fromBytes(PacketBuffer buf) {
        MicPacket soundPacket = new MicPacket();
        soundPacket.data = buf.readByteArray();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeByteArray(data);
    }
}
