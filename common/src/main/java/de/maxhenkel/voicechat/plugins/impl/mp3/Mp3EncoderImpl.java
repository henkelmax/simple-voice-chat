package de.maxhenkel.voicechat.plugins.impl.mp3;

import de.maxhenkel.lame4j.UnknownPlatformException;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusManager;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.OutputStream;

public class Mp3EncoderImpl implements Mp3Encoder, AutoCloseable {

    private final de.maxhenkel.lame4j.Mp3Encoder encoder;

    public Mp3EncoderImpl(AudioFormat audioFormat, int bitrate, int quality, OutputStream outputStream) throws IOException, UnknownPlatformException {
        encoder = new de.maxhenkel.lame4j.Mp3Encoder(audioFormat.getChannels(), (int) audioFormat.getSampleRate(), bitrate, quality, outputStream);
    }

    @Override
    public void encode(short[] samples) throws IOException {
        encoder.write(samples);
    }

    @Override
    public void close() throws IOException {
        encoder.close();
    }

    @Nullable
    public static Mp3Encoder createEncoder(AudioFormat audioFormat, int bitrate, int quality, OutputStream outputStream) {
        if (!OpusManager.useNatives()) {
            return null;
        }
        return Utils.createSafe(() -> new Mp3EncoderImpl(audioFormat, bitrate, quality, outputStream), e -> {
            Voicechat.LOGGER.error("Failed to load mp3 encoder", e);
        });
    }

}
