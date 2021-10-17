package de.maxhenkel.voicechat.voice.client;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import de.maxhenkel.rnnoise4j.RNNoise;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Denoiser {

    public static Pattern VERSIONING_PATTERN = Pattern.compile("^(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+)){0,1}){0,1}$");
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
            String version = System.getProperty("os.version");
            if (version == null) {
                return true;
            }
            Matcher matcher = VERSIONING_PATTERN.matcher(version);
            if (!matcher.matches()) {
                return true;
            }
            int major = Integer.parseInt(matcher.group("major"));
            int minor = Integer.parseInt(matcher.group("minor"));
            if (major < 10) {
                return false;
            } else if (major == 10) {
                return minor > 14;
            } else {
                return true;
            }
        }
        return true;
    }

    @Nullable
    public static Denoiser createDenoiser() {
        if (!supportsRNNoise()) {
            return null;
        }
        return Utils.createSafe(Denoiser::new);
    }

}
