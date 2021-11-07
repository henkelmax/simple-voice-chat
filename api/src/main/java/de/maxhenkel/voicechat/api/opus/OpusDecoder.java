package de.maxhenkel.voicechat.api.opus;

import javax.annotation.Nullable;

public interface OpusDecoder {

    /**
     * Decodes opus encoded audio data to 16 bit PCM audio
     *
     * @param data the opus encoded data
     * @return the 16 bit PCM audio data
     */
    short[] decode(@Nullable byte[] data);

    /**
     * Resets the decoders state
     */
    void resetState();

    /**
     * @return if the decoder is closed
     */
    boolean isClosed();

    /**
     * Closes the decoder
     * Not doing this would result in a memory leak
     */
    void close();

}
