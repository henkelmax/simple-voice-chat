package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.speex4j.UnknownPlatformException;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CrossSideManager;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.NativeUtils;

import javax.annotation.Nullable;
import java.io.IOException;

public class AutomaticGainControl extends de.maxhenkel.speex4j.AutomaticGainControl {

    public AutomaticGainControl(int frameSize, int sampleRate) throws IOException, UnknownPlatformException {
        super(frameSize, sampleRate);
    }

    @Nullable
    public static AutomaticGainControl createAgc() {
        if (!CrossSideManager.get().useNatives()) {
            return null;
        }
        return NativeUtils.createSafe(() -> new AutomaticGainControl(AudioUtils.FRAME_SIZE, AudioUtils.SAMPLE_RATE), e -> {
            Voicechat.LOGGER.warn("Failed to load automatic gain control", e);
        });
    }

    private static Boolean canUseAgc = null;

    public static synchronized boolean canUseAgc() {
        if (canUseAgc != null) {
            return canUseAgc;
        }
        AutomaticGainControl agc = createAgc();
        if (agc == null) {
            canUseAgc = false;
            return false;
        }
        agc.close();
        canUseAgc = true;
        return true;
    }

}
