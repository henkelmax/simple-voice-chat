package de.maxhenkel.voicechat.plugins.impl.mp3;

import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.natives.LameManager;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.OutputStream;

public class Mp3EncoderImpl implements Mp3Encoder, AutoCloseable {

    private final de.maxhenkel.lame4j.Mp3Encoder encoder;

    private Mp3EncoderImpl(de.maxhenkel.lame4j.Mp3Encoder encoder) {
        this.encoder = encoder;
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
        de.maxhenkel.lame4j.Mp3Encoder enc = LameManager.createEncoder(audioFormat, bitrate, quality, outputStream);
        if (enc == null) {
            return null;
        }
        return new Mp3EncoderImpl(enc);
    }

}
