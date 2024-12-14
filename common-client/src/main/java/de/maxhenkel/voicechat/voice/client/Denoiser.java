package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.rnnoise4j.UnknownPlatformException;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.ClientCrossSideManager;
import de.maxhenkel.voicechat.intercompatibility.CrossSideManager;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.io.IOException;

public class Denoiser extends de.maxhenkel.rnnoise4j.Denoiser {

    private Denoiser() throws IOException, UnknownPlatformException {
        super();
    }

    @Nullable
    public static Denoiser createDenoiser() {
        if (!CrossSideManager.get().useNatives()) {
            return null;
        }
        return Utils.createSafe(Denoiser::new, e -> {
            Voicechat.LOGGER.warn("Failed to load RNNoise", e);
        });
    }

}
