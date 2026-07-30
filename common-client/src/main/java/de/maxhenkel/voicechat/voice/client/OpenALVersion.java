package de.maxhenkel.voicechat.voice.client;

/*import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.Version;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import org.lwjgl.openal.*;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class OpenALVersion {

    @Nullable
    private static Version version;
    private static boolean checked;
*/
    /**
     * Queries the version of the OpenAL implementation.
     * If the version can't be read from the context that is current in the calling thread, it is queried on a loopback device.
     *
     * @return the OpenAL version or <code>null</code> if it could not be determined
     */
/*    @Nullable
    public static synchronized Version get() {
        if (!checked) {
            checked = true;
            version = getVersion();
        }
        return version;
    }

    @Nullable
    private static Version getVersion() {
        if (!ALC11.alcIsExtensionPresent(0L, "ALC_EXT_thread_local_context")) {
            Voicechat.LOGGER.warn("OpenAL extension 'ALC_EXT_thread_local_context' is not supported");
            return null;
        }
        if (EXTThreadLocalContext.alcGetThreadContext() != 0L && hasCapabilities()) {
            return parseVersion(AL11.alGetString(AL11.AL_VERSION));
        }
        return getVersionFromLoopbackDevice();
    }

    @Nullable
    private static Version getVersionFromLoopbackDevice() {
        if (!ALC11.alcIsExtensionPresent(0L, "ALC_SOFT_loopback")) {
            Voicechat.LOGGER.warn("OpenAL extension 'ALC_SOFT_loopback' is not supported");
            return null;
        }
        long device = SOFTLoopback.alcLoopbackOpenDeviceSOFT((ByteBuffer) null);
        if (device == 0L) {
            Voicechat.LOGGER.warn("Failed to open OpenAL loopback device");
            return null;
        }
        long previousContext = EXTThreadLocalContext.alcGetThreadContext();
        long context = 0L;
        try {
            context = ALC11.alcCreateContext(device, new int[]{
                    ALC11.ALC_FREQUENCY,
                    AudioUtils.SAMPLE_RATE,
                    SOFTLoopback.ALC_FORMAT_CHANNELS_SOFT,
                    SOFTLoopback.ALC_MONO_SOFT,
                    SOFTLoopback.ALC_FORMAT_TYPE_SOFT,
                    SOFTLoopback.ALC_SHORT_SOFT,
                    0
            });
            if (context == 0L) {
                Voicechat.LOGGER.warn("Failed to create OpenAL loopback context: {}", SoundManager.getAlcError(ALC11.alcGetError(device)));
                return null;
            }
            if (!EXTThreadLocalContext.alcSetThreadContext(context)) {
                Voicechat.LOGGER.warn("Failed to make OpenAL loopback context current: {}", SoundManager.getAlcError(ALC11.alcGetError(device)));
                return null;
            }
            AL.createCapabilities(ALC.createCapabilities(device));
            return parseVersion(AL11.alGetString(AL11.AL_VERSION));
        } catch (Throwable t) {
            Voicechat.LOGGER.warn("Failed to query the OpenAL version", t);
            return null;
        } finally {
            EXTThreadLocalContext.alcSetThreadContext(previousContext);
            AL.setCurrentThread(null);
            if (context != 0L) {
                ALC11.alcDestroyContext(context);
            }
            if (!ALC11.alcCloseDevice(device)) {
                Voicechat.LOGGER.warn("Failed to close OpenAL loopback device");
            }
        }
    }

    private static boolean hasCapabilities() {
        try {
            AL.getCapabilities();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Nullable
    private static Version parseVersion(@Nullable String versionString) {
        if (versionString == null) {
            Voicechat.LOGGER.warn("Failed to get the OpenAL version");
            return null;
        }
        Voicechat.LOGGER.debug("OpenAL version: {}", versionString);
        return Version.fromOpenALVersion(versionString);
    }

}
*/
