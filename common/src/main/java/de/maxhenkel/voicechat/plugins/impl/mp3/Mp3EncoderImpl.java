package de.maxhenkel.voicechat.plugins.impl.mp3;

import de.maxhenkel.lame4j.Lame;
import de.maxhenkel.lame4j.LameEncoder;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.OutputStream;

public class Mp3EncoderImpl implements Mp3Encoder, AutoCloseable {

    private final LameEncoder encoder;

    public Mp3EncoderImpl(AudioFormat audioFormat, int bitrate, int quality, OutputStream outputStream) throws IOException {
        Voicechat.logDebug("Initializing LAME encoder version {}", Lame.INSTANCE.get_lame_version());
        encoder = new LameEncoder(audioFormat.getChannels(), (int) audioFormat.getSampleRate(), bitrate, quality, outputStream);
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
        return Utils.createSafe(() -> {
            try {
                return new Mp3EncoderImpl(audioFormat, bitrate, quality, outputStream);
            } catch (IOException e) {
                Voicechat.LOGGER.error("Failed to initialize LAME encoder", e);
                return null;
            }
        }, e -> {
            Voicechat.LOGGER.error("Failed to load LAME encoder", e);
        });
    }

}
