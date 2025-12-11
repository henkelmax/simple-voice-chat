package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.api.mp3.Mp3Decoder;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.io.OutputStream;

public interface VoicechatApi {

    /**
     * Creates a new opus encoder.
     * Note that the encoder needs to be closed after you are finished using it.
     */
    OpusEncoder createEncoder();

    /**
     * Creates a new opus encoder.
     * Note that the encoder needs to be closed after you are finished using it.
     *
     * @param mode the opus encoder mode
     * @return the opus encoder
     */
    OpusEncoder createEncoder(OpusEncoderMode mode);

    /**
     * Creates a new opus decoder.
     * Note that the decoder needs to be closed after you are finished using it.
     *
     * @return the opus decoder
     */
    OpusDecoder createDecoder();

    /**
     * <b>NOTE</b>: This is not available for Bukkit! It will always return null.
     *
     * @param audioFormat  the audio format
     * @param bitrate      the bitrate in kbps
     * @param quality      the quality from 0 (highest) to 9 (lowest)
     * @param outputStream the output stream to write the mp3 to
     * @return the mp3 encoder or null if the encoder could not be initialized or is not available
     */
    @Nullable
    Mp3Encoder createMp3Encoder(AudioFormat audioFormat, int bitrate, int quality, OutputStream outputStream);

    /**
     * <b>NOTE</b>: This is not available for Bukkit! It will always return null.
     *
     * @param inputStream the input stream to read the mp3 from
     * @return the mp3 decoder or null if the decoder could not be initialized or is not available
     */
    @Nullable
    Mp3Decoder createMp3Decoder(InputStream inputStream);

    /**
     * @return the audio converter
     */
    AudioConverter getAudioConverter();

    /**
     * Don't forget to register your category with {@link VoicechatServerApi#registerVolumeCategory(VolumeCategory)} or {@link VoicechatClientApi#registerClientVolumeCategory(VolumeCategory)}
     *
     * @return a builder to build a category
     */
    VolumeCategory.Builder volumeCategoryBuilder();

    /**
     * <b>NOTE</b>: Voice chat plugins can change this.
     * This is only the default value.
     * This value might not be correct if you are not connected to a server.
     *
     * @return the default distance, audio from voice chat is heard from
     */
    double getVoiceChatDistance();

}
