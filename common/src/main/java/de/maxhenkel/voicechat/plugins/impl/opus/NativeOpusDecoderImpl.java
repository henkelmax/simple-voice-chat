package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.opus4j.UnknownPlatformException;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.ref.Cleaner;

public class NativeOpusDecoderImpl implements OpusDecoder {

    private static final Cleaner CLEANER = Cleaner.create();

    private final State state;
    private final Cleaner.Cleanable cleanable;

    public NativeOpusDecoderImpl(int sampleRate, int channels) throws IOException, UnknownPlatformException {
        de.maxhenkel.opus4j.OpusDecoder decoder = new de.maxhenkel.opus4j.OpusDecoder(sampleRate, channels);
        state = new State(decoder);
        cleanable = CLEANER.register(this, state);
    }

    public void setFrameSize(int frameSize) {
        state.decoder.setFrameSize(frameSize);
    }

    @Override
    public short[] decode(@Nullable byte[] data) {
        return state.decoder.decode(data);
    }

    @Override
    public short[][] decode(byte[] input, int frames) {
        return state.decoder.decode(input, frames);
    }

    @Override
    public void resetState() {
        state.decoder.resetState();
    }

    @Override
    public boolean isClosed() {
        return state.decoder.isClosed();
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    private static final class State implements Runnable {

        private final de.maxhenkel.opus4j.OpusDecoder decoder;

        private State(de.maxhenkel.opus4j.OpusDecoder decoder) {
            this.decoder = decoder;
        }

        @Override
        public void run() {
            decoder.close();
        }
    }

}
