package de.maxhenkel.voicechat.plugins.impl.audio;

import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.voice.common.AudioUtils;

public class AudioConverterImpl implements AudioConverter {

    @Override
    public short[] bytesToShorts(byte[] bytes) {
        return AudioUtils.bytesToShorts(bytes);
    }

    @Override
    public byte[] shortsToBytes(short[] shorts) {
        return AudioUtils.shortsToBytes(shorts);
    }

    @Override
    public short[] floatsToShorts(float[] floats) {
        return AudioUtils.floatsToShorts(floats);
    }

    @Override
    public float[] shortsToFloats(short[] shorts) {
        return AudioUtils.shortsToFloats(shorts);
    }

    @Override
    public byte[] floatsToBytes(float[] floats) {
        return AudioUtils.floatsToBytes(floats);
    }

    @Override
    public float[] bytesToFloats(byte[] bytes) {
        return AudioUtils.bytesToFloats(bytes);
    }
}
