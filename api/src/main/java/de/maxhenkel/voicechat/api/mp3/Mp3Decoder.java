package de.maxhenkel.voicechat.api.mp3;

import de.maxhenkel.voicechat.api.VoicechatApi;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

/**
 * You can obtain an instance of this class by calling {@link VoicechatApi#createMp3Decoder}.
 */
public interface Mp3Decoder {

    /**
     * Decodes the MP3 file and returns the decoded audio data as PCM samples.
     *
     * @return the decoded audio data as PCM samples
     * @throws IOException if an I/O error occurs
     */
    short[] decode() throws IOException;

    /**
     * Decodes the MP3 file if {@link #decode()} has not been called before.
     *
     * @return the audio format of the decoded audio data
     * @throws IOException if an I/O error occurs
     */
    AudioFormat getAudioFormat() throws IOException;

    /**
     * Decodes the MP3 file if {@link #decode()} has not been called before.
     *
     * @return the bitrate of the mp3 file
     * @throws IOException if an I/O error occurs
     */
    int getBitrate() throws IOException;

}
