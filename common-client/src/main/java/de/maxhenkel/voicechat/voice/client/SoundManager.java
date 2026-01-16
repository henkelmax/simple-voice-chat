package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerException;
import org.lwjgl.openal.*;

import javax.annotation.Nullable;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundManager {

    @Nullable
    private final String deviceName;
    private long device;
    private long context;
    private final ALCCapabilities alcCaps;
    private final ALCapabilities alCaps;
    private final float maxGain;

    public SoundManager(@Nullable String deviceName, long device, long context, ALCCapabilities alcCaps, ALCapabilities alCaps, float maxGain) {
        this.deviceName = deviceName;
        this.device = device;
        this.context = context;
        this.alcCaps = alcCaps;
        this.alCaps = alCaps;
        this.maxGain = maxGain;
    }

    public static SoundManager create() throws SpeakerException {
        return create(VoicechatClient.CLIENT_CONFIG.speaker.get());
    }

    public static SoundManager create(@Nullable String deviceName) throws SpeakerException {
        long prevContext = ALC11.alcGetCurrentContext();
        long prevDevice = (prevContext != 0L) ? ALC11.alcGetContextsDevice(prevContext) : 0L;
        try {
            long device = openSpeaker(deviceName);
            long context = ALC11.alcCreateContext(device, (IntBuffer) null);
            if (context == 0L) {
                int error = ALC11.alcGetError(device);
                ALC11.alcCloseDevice(device);
                checkAlcError(device);
                throw new SpeakerException(String.format("Failed to create OpenAL context: %s", getAlcError(error)));
            }
            if (!ALC11.alcMakeContextCurrent(context)) {
                int error = ALC11.alcGetError(device);
                ALC11.alcDestroyContext(context);
                checkAlcError(device);
                ALC11.alcCloseDevice(device);
                checkAlcError(device);
                throw new SpeakerException(String.format("Failed to make OpenAL context current: %s", getAlcError(error)));
            }

            ALCCapabilities alcCaps = ALC.createCapabilities(device);
            ALCapabilities alCaps = AL.createCapabilities(alcCaps);

            float maxGain;

            if (alCaps.AL_SOFT_gain_clamp_ex) {
                maxGain = AL11.alGetFloat(SOFTGainClampEx.AL_GAIN_LIMIT_SOFT);
                checkAlcError(device);
            } else {
                maxGain = 1F;
                Voicechat.LOGGER.warn("OpenAL extension 'AL_SOFT_gain_clamp_ex' not supported - Voice chat volume can't exceed 100%");
            }

            ClientPluginManager.instance().onCreateALContext(context, device);

            return new SoundManager(deviceName, device, context, alcCaps, alCaps, maxGain);
        } catch (SpeakerException speakerException) {
            throw speakerException;
        } catch (Throwable t) {
            throw new SpeakerException("Failed to initialize OpenAL context", t);
        } finally {
            try {
                if (prevContext != 0L) {
                    if (!ALC11.alcMakeContextCurrent(prevContext)) {
                        if (prevDevice != 0L) {
                            int error = ALC11.alcGetError(prevDevice);
                            Voicechat.LOGGER.error("Failed to restore previous OpenAL context ({}): {}", prevContext, getAlcError(error));
                        } else {
                            Voicechat.LOGGER.error("Failed to restore previous OpenAL context ({}): Device not found", prevContext);
                        }
                    } else {
                        if (prevDevice != 0L) {
                            ALCCapabilities prevAlcCaps = ALC.createCapabilities(prevDevice);
                            AL.createCapabilities(prevAlcCaps);
                        }
                    }
                }
            } catch (Throwable t) {
                Voicechat.LOGGER.warn("Failed to restore previous OpenAL context", t);
            }
        }
    }

    public void close() {
        if (!isClosed()) {
            ClientPluginManager.instance().onDestroyALContext(context, device);
        }
        if (context != 0L) {
            ALC11.alcDestroyContext(context);
            checkAlcError(device);
        }
        if (device != 0L) {
            if (!ALC11.alcCloseDevice(device)) {
                checkAlcError(device);
            }
        }
        context = 0;
        device = 0;
    }

    public float getMaxGain() {
        return maxGain;
    }

    public boolean isClosed() {
        return context == 0 || device == 0;
    }

    private static long openSpeaker(@Nullable String name) throws SpeakerException {
        try {
            return tryOpenSpeaker(name);
        } catch (SpeakerException e) {
            if (name == null) {
                throw e;
            }
            Voicechat.LOGGER.warn("Failed to open audio device '{}', falling back to default", name);
            return tryOpenSpeaker(null);
        }
    }

    private static long tryOpenSpeaker(@Nullable String string) throws SpeakerException {
        long device = ALC11.alcOpenDevice(string);
        if (device == 0L) {
            throw new SpeakerException(String.format("Failed to open audio device: Audio device '%s' not found", string));
        }
        int error = ALC11.alcGetError(device);
        if (error != ALC11.ALC_NO_ERROR) {
            if (!ALC11.alcCloseDevice(device)) {
                Voicechat.LOGGER.warn("Failed to close audio device");
            }
            throw new SpeakerException(String.format("Failed to open audio device: %s", getAlcError(error)));
        }
        return device;
    }

    public static List<String> getAllSpeakers() {
        List<String> devices = null;
        if (canEnumerateAll()) {
            devices = ALUtil.getStringList(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER);
        } else {
            Voicechat.LOGGER.warn("Extension ALC_ENUMERATE_ALL_EXT is not present");
        }
        boolean canEnumerate = canEnumerate();
        if (devices == null && !canEnumerate) {
            Voicechat.LOGGER.warn("Extension ALC_ENUMERATION_EXT is not present");
        }
        if (devices == null && canEnumerate) {
            devices = ALUtil.getStringList(0L, ALC11.ALC_DEVICE_SPECIFIER);
        }
        if (devices == null) {
            devices = Collections.emptyList();
        }
        return devices;
    }

    public static boolean canEnumerateAll() {
        return ALC11.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT");
    }

    public static boolean canEnumerate() {
        return ALC11.alcIsExtensionPresent(0L, "ALC_ENUMERATION_EXT");
    }

    public void runInContext(Executor executor, Runnable runnable) {
        long time = System.currentTimeMillis();
        executor.execute(() -> {
            long diff = System.currentTimeMillis() - time;
            if (diff > 20 || (diff >= 5 && Voicechat.debugMode())) {
                Voicechat.LOGGER.warn("Sound executor delay: {} ms!", diff);
            }
            if (openContext()) {
                runnable.run();
                closeContext();
            }
        });
    }

    public boolean openContext() {
        if (context == 0) {
            return false;
        }
        boolean success = EXTThreadLocalContext.alcSetThreadContext(context);
        checkAlcError(device);
        return success;
    }

    public void closeContext() {
        EXTThreadLocalContext.alcSetThreadContext(0L);
        checkAlcError(device);
    }

    public static boolean checkAlError() {
        int error = AL11.alGetError();
        if (error == AL11.AL_NO_ERROR) {
            return false;
        }
        StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
        Voicechat.LOGGER.error("Voicechat sound manager AL error: {}.{}[{}] {}", stack.getClassName(), stack.getMethodName(), stack.getLineNumber(), getAlError(error));
        return true;
    }

    public static boolean checkAlcError(long device) {
        int error = ALC11.alcGetError(device);
        if (error == ALC11.ALC_NO_ERROR) {
            return false;
        }
        StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
        Voicechat.LOGGER.error("Voicechat sound manager ALC error: {}.{}[{}] {}", stack.getClassName(), stack.getMethodName(), stack.getLineNumber(), getAlcError(error));
        return true;
    }

    public static String getAlError(int errorCode) {
        switch (errorCode) {
            case AL11.AL_INVALID_NAME:
                return "Invalid name";
            case AL11.AL_INVALID_ENUM:
                return "Invalid enum ";
            case AL11.AL_INVALID_VALUE:
                return "Invalid value";
            case AL11.AL_INVALID_OPERATION:
                return "Invalid operation";
            case AL11.AL_OUT_OF_MEMORY:
                return "Out of memory";
            default:
                return String.format("Error %#X", errorCode);
        }
    }

    public static String getAlcError(int i) {
        switch (i) {
            case ALC11.ALC_INVALID_DEVICE:
                return "Invalid device";
            case ALC11.ALC_INVALID_CONTEXT:
                return "Invalid context";
            case ALC11.ALC_INVALID_ENUM:
                return "Invalid enum";
            case ALC11.ALC_INVALID_VALUE:
                return "Invalid value";
            case ALC11.ALC_OUT_OF_MEMORY:
                return "Out of memory";
            default:
                return "Unknown error";
        }
    }

    private static final Pattern DEVICE_NAME = Pattern.compile("^(?:OpenAL.+?on )?(.*)$");

    public static String cleanDeviceName(String name) {
        Matcher matcher = DEVICE_NAME.matcher(name);
        if (!matcher.matches()) {
            return name;
        }
        return matcher.group(1);
    }

}
