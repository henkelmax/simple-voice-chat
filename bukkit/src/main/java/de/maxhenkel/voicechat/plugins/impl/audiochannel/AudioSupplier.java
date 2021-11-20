package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import java.util.Arrays;
import java.util.function.Supplier;

public class AudioSupplier implements Supplier<short[]> {

    private final short[] audioData;
    private final short[] frame;
    private int framePosition;

    public AudioSupplier(short[] audioData) {
        this.audioData = audioData;
        this.frame = new short[AudioPlayerImpl.FRAME_SIZE];
    }

    @Override
    public short[] get() {
        if (framePosition >= audioData.length) {
            return null;
        }

        Arrays.fill(frame, (short) 0);
        System.arraycopy(audioData, framePosition, frame, 0, Math.min(frame.length, audioData.length - framePosition));
        framePosition += frame.length;
        return frame;
    }
}
