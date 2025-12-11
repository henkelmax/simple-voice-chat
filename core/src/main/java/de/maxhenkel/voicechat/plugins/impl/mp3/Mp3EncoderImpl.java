package de.maxhenkel.voicechat.plugins.impl.mp3;

import de.maxhenkel.lame4j.UnknownPlatformException;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Cleaner;

public class Mp3EncoderImpl implements Mp3Encoder, AutoCloseable {

    private static final Cleaner CLEANER = Cleaner.create();

    private final State state;
    private final Cleaner.Cleanable cleanable;

    public Mp3EncoderImpl(AudioFormat audioFormat, int bitrate, int quality, OutputStream outputStream) throws IOException, UnknownPlatformException {
        de.maxhenkel.lame4j.Mp3Encoder enc = new de.maxhenkel.lame4j.Mp3Encoder(audioFormat.getChannels(), (int) audioFormat.getSampleRate(), bitrate, quality, outputStream);
        state = new State(enc);
        cleanable = CLEANER.register(this, state);
    }

    @Override
    public void encode(short[] samples) throws IOException {
        state.encoder.write(samples);
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    private static final class State implements Runnable {

        private final de.maxhenkel.lame4j.Mp3Encoder encoder;

        private State(de.maxhenkel.lame4j.Mp3Encoder encoder) {
            this.encoder = encoder;
        }

        @Override
        public void run() {
            try {
                encoder.close();
            } catch (IOException e) {
                Voicechat.LOGGER.error("Failed to close MP3 encoder", e);
            }
        }
    }

}
