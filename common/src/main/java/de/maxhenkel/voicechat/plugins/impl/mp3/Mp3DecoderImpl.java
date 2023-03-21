package de.maxhenkel.voicechat.plugins.impl.mp3;

import de.maxhenkel.lame4j.ShortArrayBuffer;
import de.maxhenkel.lame4j.UnknownPlatformException;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.mp3.Mp3Decoder;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusManager;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

public class Mp3DecoderImpl implements Mp3Decoder {

    private final de.maxhenkel.lame4j.Mp3Decoder decoder;
    private IOException decodeError;
    @Nullable
    private short[] samples;
    @Nullable
    private AudioFormat audioFormat;

    public Mp3DecoderImpl(InputStream inputStream) throws IOException, UnknownPlatformException {
        decoder = new de.maxhenkel.lame4j.Mp3Decoder(inputStream);
    }

    private void decodeIfNecessary() throws IOException {
        if (decodeError != null) {
            throw decodeError;
        }
        try {
            if (samples == null) {
                ShortArrayBuffer sampleBuffer = new ShortArrayBuffer(2048);
                while (true) {
                    short[] samples = decoder.decodeNextFrame();
                    if (samples == null) {
                        break;
                    }
                    sampleBuffer.writeShorts(samples);
                }
                samples = sampleBuffer.toShortArray();
                audioFormat = decoder.createAudioFormat();
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
        return decoder.getBitRate();
    }

    @Nullable
    public static Mp3Decoder createDecoder(InputStream inputStream) {
        if (!OpusManager.useNatives()) {
            return null;
        }
        return Utils.createSafe(() -> new Mp3DecoderImpl(inputStream), e -> {
            Voicechat.LOGGER.error("Failed to load mp3 decoder", e);
        });
    }

}
