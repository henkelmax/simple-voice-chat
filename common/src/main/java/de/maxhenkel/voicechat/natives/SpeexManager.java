package de.maxhenkel.voicechat.natives;

import de.maxhenkel.speex4j.AutomaticGainControl;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.AudioUtils;

import javax.annotation.Nullable;

public class SpeexManager extends NativeValidator {

    public static final int TARGET = AudioUtils.dbSample(-5D);

    @Override
    protected void runValidation() throws Throwable {
        try (AutomaticGainControl agc = new AutomaticGainControl(AudioUtils.FRAME_SIZE, AudioUtils.SAMPLE_RATE)) {
            agc.setTarget(TARGET);
            agc.agc(new short[AudioUtils.FRAME_SIZE]);
        }
    }

    @Override
    protected String getNativeName() {
        return "Speex";
    }

    @Nullable
    public static AutomaticGainControl createAgc() {
        SpeexManager instance = instance();
        if (!instance.canUse()) {
            return null;
        }
        return NativeUtils.createSafe(() -> {
            AutomaticGainControl agc = new AutomaticGainControl(AudioUtils.FRAME_SIZE, AudioUtils.SAMPLE_RATE);
            agc.setTarget(TARGET);
            return agc;
        }, e -> {
            instance.setFailed(e.getMessage());
            Voicechat.LOGGER.warn("Failed to load Speex", e);
        });
    }

    public static boolean canUseAgc() {
        return instance().canUse();
    }

    public static void init() {
        instance().initialize();
    }

    private static SpeexManager instance;

    private static synchronized SpeexManager instance() {
        if (instance == null) {
            instance = new SpeexManager();
        }
        return instance;
    }

}
