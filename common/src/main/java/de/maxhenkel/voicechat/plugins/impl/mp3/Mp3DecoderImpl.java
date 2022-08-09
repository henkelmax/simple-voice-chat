package de.maxhenkel.voicechat.plugins.impl.mp3;

import de.maxhenkel.lame4j.Lame;
import de.maxhenkel.lame4j.LameDecoder;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.mp3.Mp3Decoder;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

public class Mp3DecoderImpl implements Mp3Decoder {

    private final LameDecoder decoder;
    private IOException decodeError;
    @Nullable
    private short[] samples;
    @Nullable
    private AudioFormat audioFormat;

    public Mp3DecoderImpl(InputStream inputStream) {
        Voicechat.logDebug("Initializing LAME decoder version {}", Lame.INSTANCE.get_lame_version());
        decoder = new LameDecoder(inputStream);
    }

    private void decodeIfNecessary() throws IOException {
        if (decodeError != null) {
            throw decodeError;
        }
        try {
            if (samples == null) {
                samples = decoder.decode();
                audioFormat = decoder.format();
            }
        } catch (IOException e) {
            decodeError = e;
            throw e;
        }
    }

    @Override
    public short[] decode() throws IOException {
        decodeIfNecessary();
        return samples;
    }

    @Override
    public AudioFormat getAudioFormat() throws IOException {
        decodeIfNecessary();
        return audioFormat;
    }

    @Override
    public int getBitrate() throws IOException {
        decodeIfNecessary();
        return decoder.getBitrate();
    }

    @Nullable
    public static Mp3Decoder createDecoder(InputStream inputStream) {
        return Utils.createSafe(() -> new Mp3DecoderImpl(inputStream), e -> {
            Voicechat.LOGGER.error("Failed to load LAME decoder: {}", e.getMessage());
        });
    }

}
