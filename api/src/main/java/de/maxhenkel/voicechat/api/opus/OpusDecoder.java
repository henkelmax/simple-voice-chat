package de.maxhenkel.voicechat.api.opus;

import de.maxhenkel.voicechat.api.VoicechatApi;

import javax.annotation.Nullable;

/**
 * Instances can be obtained by calling {@link VoicechatApi#createDecoder()}.
 */
public interface OpusDecoder {

    /**
     * Decodes opus encoded audio data to 16 bit PCM audio.
     *
     * @param data the opus encoded data or <code>null</code> to do PLC
     * @return the 16 bit PCM audio data
     */
    short[] decode(@Nullable byte[] data);

    /**
     * Decodes the provided packet and recovers previous lost frames using FEC.
     *
     * @param input  the input packet
     * @param frames the number of frames to return (min 1 for just the current frame)
     * @return an array containing the decoded frames - the length of the array is equal to {@param frames}
     */
    short[][] decode(byte[] input, int frames);

    /**
     * Resets the decoders state.
     */
    void resetState();

    /**
     * @return if the decoder is closed
     */
    boolean isClosed();

    /**
     * Closes the decoder.
     * <b>NOTE</b>: Not closing encoders will cause a memory leak in voice chat versions <code>&lt;2.6.3</code>!
     */
    void close();

}
