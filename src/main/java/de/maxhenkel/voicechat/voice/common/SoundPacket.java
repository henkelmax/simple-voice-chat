package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

import javax.sound.sampled.AudioFormat;

public class SoundPacket implements Packet<SoundPacket> {

    public static final AudioFormat DEFAULT_FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100F, 16, 1, 2, 44100F, false);
    public static final int DEFAULT_DATA_LENGTH = 20_000;

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
