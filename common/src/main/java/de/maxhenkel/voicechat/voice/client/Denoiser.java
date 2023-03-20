package de.maxhenkel.voicechat.voice.client;

import com.sun.jna.Platform;
import de.maxhenkel.rnnoise4j.UnknownPlatformException;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.macos.VersionCheck;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.io.IOException;

public class Denoiser extends de.maxhenkel.rnnoise4j.Denoiser {

    private Denoiser() throws IOException, UnknownPlatformException {
        super();
    }

    public static boolean supportsRNNoise() {
        //TODO Fix check
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
            Voicechat.LOGGER.warn("Failed to load RNNoise", e);
        });
    }

}
