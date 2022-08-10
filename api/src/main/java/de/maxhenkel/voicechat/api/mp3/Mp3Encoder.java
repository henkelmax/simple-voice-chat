package de.maxhenkel.voicechat.api.mp3;

import de.maxhenkel.voicechat.api.VoicechatApi;

import java.io.IOException;

/**
 * You can obtain an instance of this class by calling {@link VoicechatApi#createMp3Encoder}.
 */
public interface Mp3Encoder {

    /**
     * Encodes the given PCM samples and writes it to the provided output stream.
     *
     * @param samples the PCM samples to encode
     * @throws IOException if an I/O error occurs
     */
    void encode(short[] samples) throws IOException;

    /**
     * Closes the encoder and flushes the output stream.
     * Also writes leftover mp3 data to the output stream.
     *
     * <b>NOTE</b>: Not closing encoders will cause a memory leak!
     *
     * @throws IOException if an I/O error occurs
     */
    void close() throws IOException;

}
