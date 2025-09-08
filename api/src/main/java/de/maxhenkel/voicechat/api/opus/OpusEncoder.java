package de.maxhenkel.voicechat.api.opus;

import de.maxhenkel.voicechat.api.VoicechatApi;

/**
 * Instances can be obtained by calling {@link VoicechatApi#createEncoder()}.
 */
public interface OpusEncoder {

    /**
     * Encodes 16 bit PCM audio with opus.
     *
     * @param rawAudio the raw 16 bit PCM audio
     * @return the opus encoded data
     */
    byte[] encode(short[] rawAudio);

    /**
     * Resets the encoders state.
     */
    void resetState();

    /**
     * @return if the encoder is closed
     */
    boolean isClosed();

    /**
     * Closes the encoder.
     * <b>NOTE</b>: Not closing encoders will cause a memory leak in voice chat versions <code>&lt;2.6.3</code>!
     */
    void close();

}
