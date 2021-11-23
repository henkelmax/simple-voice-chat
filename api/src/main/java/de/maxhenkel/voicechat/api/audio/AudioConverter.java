package de.maxhenkel.voicechat.api.audio;

import de.maxhenkel.voicechat.api.VoicechatApi;

/**
 * A utility class to convert audio between different representations.
 * <br/>
 * Can be obtained by calling {@link VoicechatApi#getAudioConverter()}.
 * <br/>
 * <br/>
 * <b>NOTE</b>: Everything is assumed to be 16 bit PCM audio.
 */
public interface AudioConverter {

    short[] bytesToShorts(byte[] bytes);

    byte[] shortsToBytes(short[] shorts);

    short[] floatsToShorts(float[] floats);

    float[] shortsToFloats(short[] shorts);

    byte[] floatsToBytes(float[] floats);

    float[] bytesToFloats(byte[] bytes);

}
