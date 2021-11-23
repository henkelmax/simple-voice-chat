package de.maxhenkel.voicechat.plugins.impl.audio;

import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.voice.common.Utils;

public class AudioConverterImpl implements AudioConverter {

    @Override
    public short[] bytesToShorts(byte[] bytes) {
        return Utils.bytesToShorts(bytes);
    }

    @Override
    public byte[] shortsToBytes(short[] shorts) {
        return Utils.shortsToBytes(shorts);
    }

    @Override
    public short[] floatsToShorts(float[] floats) {
        return Utils.floatsToShorts(floats);
    }

    @Override
    public float[] shortsToFloats(short[] shorts) {
        return Utils.shortsToFloats(shorts);
    }

    @Override
    public byte[] floatsToBytes(float[] floats) {
        return Utils.floatsToBytes(floats);
    }

    @Override
    public float[] bytesToFloats(byte[] bytes) {
        return Utils.bytesToFloats(bytes);
    }
}
