package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketByteBuf;

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
    public MicPacket fromBytes(PacketByteBuf buf) {
        MicPacket soundPacket = new MicPacket();
        soundPacket.data = buf.readByteArray();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeByteArray(data);
    }
}
