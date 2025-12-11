package de.maxhenkel.voicechat.plugins.impl.opus;

import de.maxhenkel.opus4j.UnknownPlatformException;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;

import java.io.IOException;
import java.lang.ref.Cleaner;

public class NativeOpusEncoderImpl implements OpusEncoder {

    private static final Cleaner CLEANER = Cleaner.create();

    private final State state;
    private final Cleaner.Cleanable cleanable;

    public NativeOpusEncoderImpl(int sampleRate, int channels, de.maxhenkel.opus4j.OpusEncoder.Application application) throws IOException, UnknownPlatformException {
        de.maxhenkel.opus4j.OpusEncoder encoder = new de.maxhenkel.opus4j.OpusEncoder(sampleRate, channels, application);
        // This enables in-band FEC
        encoder.setMaxPacketLossPercentage(0.05F);
        state = new State(encoder);
        cleanable = CLEANER.register(this, state);
    }

    public void setMaxPayloadSize(int maxPayloadSize) {
        state.encoder.setMaxPayloadSize(maxPayloadSize);
    }

    @Override
    public byte[] encode(short[] rawAudio) {
        return state.encoder.encode(rawAudio);
    }

    @Override
    public void resetState() {
        state.encoder.resetState();
    }

    @Override
    public boolean isClosed() {
        return state.encoder.isClosed();
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    private static final class State implements Runnable {

        private final de.maxhenkel.opus4j.OpusEncoder encoder;

        private State(de.maxhenkel.opus4j.OpusEncoder encoder) {
            this.encoder = encoder;
        }

        @Override
        public void run() {
            encoder.close();
        }
    }

}
