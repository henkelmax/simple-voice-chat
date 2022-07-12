package de.maxhenkel.voicechat.voice.client;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import de.maxhenkel.rnnoise4j.RNNoise;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.macos.VersionCheck;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;

public class Denoiser {

    private static final int FRAME_SIZE = 480;

    private final Pointer state;
    private boolean closed;

    private Denoiser() {
        state = RNNoise.INSTANCE.rnnoise_create(null);
    }

    public short[] denoise(short[] audio) {
        if (closed) {
            throw new IllegalStateException("Tried to denoise with a closed denoiser");
        }
        float[] data = Utils.shortsToFloats(audio);
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
        return Utils.floatsToShorts(denoised);
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        RNNoise.INSTANCE.rnnoise_destroy(state);
    }

    public static boolean supportsRNNoise() {
        if (Platform.isMac()) {
            return VersionCheck.isMinimumVersion(10, 15, 0);
        }
        return true;
    }

    @Nullable
    public static Denoiser createDenoiser() {
        if (!VoicechatClient.CLIENT_CONFIG.useNatives.get()) {
            return null;
        }
        if (!supportsRNNoise()) {
            return null;
        }
        return Utils.createSafe(Denoiser::new, e -> {
            Voicechat.LOGGER.warn("Failed to load RNNoise: {}", e.getMessage());
        });
    }

}
