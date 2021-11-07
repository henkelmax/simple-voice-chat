package de.maxhenkel.voicechat.api.opus;

public interface OpusEncoder {

    /**
     * Encodes 16 bit PCM audio with opus
     *
     * @param rawAudio the raw 16 bit PCM audio
     * @return the opus encoded data
     */
    byte[] encode(short[] rawAudio);

    /**
     * Resets the encoders state
     */
    void resetState();

    /**
     * @return if the encoder is closed
     */
    boolean isClosed();

    /**
     * Closes the encoder
     * Not doing this would result in a memory leak
     */
    void close();

}
