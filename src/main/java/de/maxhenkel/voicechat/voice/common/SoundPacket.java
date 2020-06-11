package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

public class SoundPacket implements Packet<SoundPacket> {

    private byte[] data;

    public SoundPacket(byte[] data) {
        this.data = data;
    }

    public SoundPacket() {

    }

    public byte[] getData() {
        return data;
    }

    @Override
    public SoundPacket fromBytes(PacketBuffer buf) {
        SoundPacket soundPacket = new SoundPacket();
        soundPacket.data = buf.readByteArray();
        return soundPacket;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeByteArray(data);
    }
}
