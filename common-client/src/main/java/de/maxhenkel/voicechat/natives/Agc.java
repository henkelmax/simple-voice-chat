package de.maxhenkel.voicechat.natives;

import de.maxhenkel.speex4j.AutomaticGainControl;
import de.maxhenkel.speex4j.UnknownPlatformException;

import java.io.IOException;
import java.lang.ref.Cleaner;

public class Agc implements AutoCloseable {

    private static final Cleaner CLEANER = Cleaner.create();

    private final State state;
    private final Cleaner.Cleanable cleanable;

    public Agc(int frameSize, int sampleRate) throws IOException, UnknownPlatformException {
        AutomaticGainControl agc = new AutomaticGainControl(frameSize, sampleRate);
        state = new State(agc);
        cleanable = CLEANER.register(this, state);
    }

    public void setTarget(int target) {
        state.agc.setTarget(target);
    }

    public int getTarget() {
        return state.agc.getTarget();
    }

    public void setMaxGain(int maxGain) {
        state.agc.setMaxGain(maxGain);
    }

    public int getMaxGain() {
        return state.agc.getMaxGain();
    }

    public void setIncrement(int increment) {
        state.agc.setIncrement(increment);
    }

    public int getIncrement() {
        return state.agc.getIncrement();
    }

    public void setDecrement(int decrement) {
        state.agc.setDecrement(decrement);
    }

    public int getDecrement() {
        return state.agc.getDecrement();
    }

    public void setVadProbStart(int probStart) {
        state.agc.setVadProbStart(probStart);
    }

    public int getVadProbStart() {
        return state.agc.getVadProbStart();
    }

    public void setVadProbContinue(int probContinue) {
        state.agc.setVadProbContinue(probContinue);
    }

    public int getVadProbContinue() {
        return state.agc.getVadProbContinue();
    }

    public boolean agc(short[] input) {
        return state.agc.agc(input);
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    public boolean isClosed() {
        return state.agc.isClosed();
    }

    private static final class State implements Runnable {

        private final AutomaticGainControl agc;

        private State(AutomaticGainControl agc) {
            this.agc = agc;
        }

        @Override
        public void run() {
            agc.close();
        }
    }

}
