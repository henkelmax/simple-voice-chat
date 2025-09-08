package de.maxhenkel.voicechat.natives;

import de.maxhenkel.rnnoise4j.UnknownPlatformException;

import java.io.IOException;
import java.lang.ref.Cleaner;

public class Denoiser implements AutoCloseable {

    private static final Cleaner CLEANER = Cleaner.create();

    private final State state;
    private final Cleaner.Cleanable cleanable;

    public Denoiser() throws IOException, UnknownPlatformException {
        de.maxhenkel.rnnoise4j.Denoiser denoiser = new de.maxhenkel.rnnoise4j.Denoiser();
        state = new State(denoiser);
        cleanable = CLEANER.register(this, state);
    }

    public int getFrameSize() {
        return state.denoiser.getFrameSize();
    }

    public short[] denoise(short[] input) {
        return state.denoiser.denoise(input);
    }

    public float denoiseInPlace(short[] input) {
        return state.denoiser.denoiseInPlace(input);
    }

    public float getSpeechProbability(short[] input) {
        return state.denoiser.getSpeechProbability(input);
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    public boolean isClosed() {
        return state.denoiser.isClosed();
    }

    private static final class State implements Runnable {

        private final de.maxhenkel.rnnoise4j.Denoiser denoiser;

        private State(de.maxhenkel.rnnoise4j.Denoiser denoiser) {
            this.denoiser = denoiser;
        }

        @Override
        public void run() {
            denoiser.close();
        }
    }

}
