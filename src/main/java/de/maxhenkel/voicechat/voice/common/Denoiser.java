package de.maxhenkel.voicechat.voice.common;

import com.sun.jna.Pointer;
import de.maxhenkel.rnnoise4j.RNNoise;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class Denoiser {

    private static final int FRAME_SIZE = 480;

    private final Pointer state;

    private Denoiser() {
        state = RNNoise.INSTANCE.rnnoise_create(null);
    }

    public byte[] denoise(byte[] audio) {
        float[] data = Utils.bytesToFloat(audio);
        if (data.length % FRAME_SIZE != 0) {
            throw new IllegalArgumentException("Denoising data frame size is not divisible by 480");
        }
        float[] chunk = new float[FRAME_SIZE];
        float[] denoisedChunk = new float[FRAME_SIZE];
        float[] denoised = new float[data.length];
        for (int i = 0; i < data.length / FRAME_SIZE; i++) {
            System.arraycopy(data, FRAME_SIZE * i, chunk, 0, FRAME_SIZE);
            RNNoise.INSTANCE.rnnoise_process_frame(state, denoisedChunk, chunk);
            System.arraycopy(denoisedChunk, 0, denoised, FRAME_SIZE * i, FRAME_SIZE);
        }
        return Utils.floatToBytes(denoised);
    }

    public void close() {
        RNNoise.INSTANCE.rnnoise_destroy(state);
    }

    @Nullable
    public static Denoiser createDenoiser() {
        AtomicReference<Denoiser> denoiser = new AtomicReference<>();
        Thread t = new Thread(() -> {
            denoiser.set(new Denoiser());
        });
        t.start();

        try {
            t.join(1000);
        } catch (InterruptedException e) {
            return null;
        }
        return denoiser.get();
    }

}
