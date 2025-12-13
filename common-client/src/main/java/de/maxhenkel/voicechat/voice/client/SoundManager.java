package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerException;
import org.lwjgl.openal.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundManager {

    @Nullable
    private final String deviceName;
    private ALCdevice device;
    private ALCcontext context;
    /*private final ALCCapabilities alcCaps;
    private final ALCapabilities alCaps;*/
    private final float maxGain;

    @Deprecated
    public SoundManager(@Nullable String deviceName, ALCdevice device, ALCcontext context/*, ALCCapabilities alcCaps, ALCapabilities alCaps*/, float maxGain) {
        this.deviceName = deviceName;
        this.device = device;
        this.context = context;
        /*this.alcCaps = alcCaps;
        this.alCaps = alCaps;*/
        this.maxGain = maxGain;
    }

    public static SoundManager create() throws SpeakerException {
        //return create(VoicechatClient.CLIENT_CONFIG.speaker.get());
        return null;
    }

    /*public static SoundManager create(@Nullable String deviceName) throws SpeakerException {
        long prevContext = ALC10.alcGetCurrentContext();
        long prevDevice = (prevContext != 0L) ? ALC11.alcGetContextsDevice(prevContext) : 0L;
        try {
            long device = openSpeaker(deviceName);
            long context = ALC10.alcCreateContext(device, (IntBuffer) null);
            if (context == 0L) {
                int error = ALC11.alcGetError(device);
                ALC11.alcCloseDevice(device);
                checkAlcError(device);
                throw new SpeakerException("Failed to create OpenAL context: %s".formatted(getAlcError(error)));
            }
            if (!ALC11.alcMakeContextCurrent(context)) {
                int error = ALC11.alcGetError(device);
                ALC11.alcDestroyContext(context);
                checkAlcError(device);
                ALC11.alcCloseDevice(device);
                checkAlcError(device);
                throw new SpeakerException("Failed to make OpenAL context current: %s".formatted(getAlcError(error)));
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

            ClientPluginManager.instance().onCreateALContext(getContextAddress(context), getDeviceAddress(device));

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
    }*/

    public void close() {
        ClientPluginManager.instance().onDestroyALContext(getContextAddress(context), getDeviceAddress(device));
        if (context != null) {
            ALC10.alcDestroyContext(context);
            checkAlcError(device);
        }
        if (device != null) {
            ALC10.alcCloseDevice(device);
            checkAlcError(device);
        }
        context = null;
        device = null;
    }

    public float getMaxGain() {
        return maxGain;
    }

    public boolean isClosed() {
        return context == null || device == null;
    }

    private static ALCdevice openSpeaker(@Nullable String name) throws SpeakerException {
        try {
            return tryOpenSpeaker(name);
        } catch (SpeakerException e) {
            if (name != null) {
                Voicechat.LOGGER.warn("Failed to open audio device '{}', falling back to default", name);
            }
            try {
                return tryOpenSpeaker(getDefaultSpeaker());
            } catch (SpeakerException ex) {
                return tryOpenSpeaker(null);
            }
        }
    }

    private static ALCdevice tryOpenSpeaker(@Nullable String string) throws SpeakerException {
        ALCdevice l = ALC10.alcOpenDevice(string);
        if (l == null) {
            throw new SpeakerException("Failed to open audio device: Audio device not found");
        }
        int error = ALC10.alcGetError(l);
        if (error != ALC10.ALC_NO_ERROR) {
            if (!ALC10.alcCloseDevice(l)) {
                Voicechat.LOGGER.warn("Failed to close audio device");
            }
            throw new SpeakerException(String.format("Failed to open audio device: %s", getAlcError(error)));
        }
        return l;
    }

    @Nullable
    public static String getDefaultSpeaker() {
        if (!canEnumerate()) {
            return null;
        }
        String defaultSpeaker = ALC10.alcGetString(null, ALC11.ALC_ALL_DEVICES_SPECIFIER);
        checkAlcError(null);
        return defaultSpeaker;
    }

    public static List<String> getAllSpeakers() {
        if (!canEnumerate()) {
            return Collections.emptyList();
        }
        //TODO Fix audio devices
        List<String> devices = new ArrayList<>();//ALUtil.getStringList(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER);
        checkAlcError(null);
        return devices == null ? Collections.emptyList() : devices;
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
        if (context == null) {
            return false;
        }
        // TODO Fix threaded AL audio
        boolean success = true;/*EXTThreadLocalContext.alcSetThreadContext(context);
        checkAlcError(device);*/
        return success;
    }

    public void closeContext() {
        // TODO Fix threaded AL audio
        // EXTThreadLocalContext.alcSetThreadContext(0L);
        checkAlcError(device);
    }

    public static boolean checkAlError() {
        int error = AL10.alGetError();
        if (error == AL10.AL_NO_ERROR) {
            return false;
        }
        StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
        Voicechat.LOGGER.error("Voicechat sound manager AL error: {}.{}[{}] {}", stack.getClassName(), stack.getMethodName(), stack.getLineNumber(), getAlError(error));
        return true;
    }

    public static boolean checkAlcError(@Nullable ALCdevice device) {
        int error = ALC10.alcGetError(device);
        if (error == ALC10.ALC_NO_ERROR) {
            return false;
        }
        StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
        Voicechat.LOGGER.error("Voicechat sound manager ALC error: {}.{}[{}] {}", stack.getClassName(), stack.getMethodName(), stack.getLineNumber(), getAlcError(error));
        return true;
    }

    private static String getAlError(int errorCode) {
        switch (errorCode) {
            case AL10.AL_INVALID_NAME:
                return "Invalid name";
            case AL10.AL_INVALID_ENUM:
                return "Invalid enum ";
            case AL10.AL_INVALID_VALUE:
                return "Invalid value";
            case AL10.AL_INVALID_OPERATION:
                return "Invalid operation";
            case AL10.AL_OUT_OF_MEMORY:
                return "Out of memory";
            default:
                return "Error %#X".formatted(errorCode);
        }
    }

    public static String getAlcError(int i) {
        switch (i) {
            case ALC10.ALC_INVALID_DEVICE:
                return "Invalid device";
            case ALC10.ALC_INVALID_CONTEXT:
                return "Invalid context";
            case ALC10.ALC_INVALID_ENUM:
                return "Invalid enum";
            case ALC10.ALC_INVALID_VALUE:
                return "Invalid value";
            case ALC10.ALC_OUT_OF_MEMORY:
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

    public static boolean canEnumerate() {
        //TODO Fix device enumeration
        /*boolean present = ALC10.alcIsExtensionPresent(null, "ALC_ENUMERATE_ALL_EXT");
        checkAlcError(null);
        return present;*/
        return false;
    }

    public static long getContextAddress(ALCcontext context) {
        try {
            Field c = context.getClass().getDeclaredField("context");
            c.setAccessible(true);
            return (long) c.get(context);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public static long getDeviceAddress(ALCdevice device) {
        try {
            Field c = device.getClass().getDeclaredField("device");
            c.setAccessible(true);
            return (long) c.get(device);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

}