package de.maxhenkel.voicechat.voice.common;

import javax.sound.sampled.AudioFormat;
import java.io.Serializable;

public class SoundPacket implements Serializable {

    public static final AudioFormat DEFAULT_FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100F, 16, 1, 2, 44100F, false);
    public static final int DEFAULT_DATA_LENGTH = 20_000;

    private byte[] data;

    public SoundPacket(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

}
