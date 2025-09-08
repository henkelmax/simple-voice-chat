package de.maxhenkel.voicechat.natives;

import de.maxhenkel.voicechat.Voicechat;

import javax.annotation.Nullable;

public class RNNoiseManager extends NativeValidator {

    @Override
    protected void runValidation() throws Throwable {
        try (Denoiser denoiser = new Denoiser()) {
            denoiser.denoiseInPlace(new short[denoiser.getFrameSize()]);
            denoiser.denoise(new short[denoiser.getFrameSize()]);
        }
    }

    @Override
    protected String getNativeName() {
        return "RNNoise";
    }

    @Nullable
    public static Denoiser createDenoiser() {
        RNNoiseManager instance = instance();
        if (!instance.canUse()) {
            return null;
        }
        return NativeUtils.createSafe(Denoiser::new, e -> {
            instance.setFailed(e.getMessage());
            Voicechat.LOGGER.warn("Failed to load RNNoise", e);
        });
    }

    public static boolean canUseDenoiser() {
        return instance().canUse();
    }

    public static void init() {
        instance().initialize();
    }

    public static boolean isFailed() {
        return !instance().canUse();
    }

    public static String getFailedMessage() {
        return instance().getMessage();
    }

    private static RNNoiseManager instance;

    private static synchronized RNNoiseManager instance() {
        if (instance == null) {
            instance = new RNNoiseManager();
        }
        return instance;
    }

}
