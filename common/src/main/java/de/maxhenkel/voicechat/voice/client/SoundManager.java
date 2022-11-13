package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.plugins.PluginManager;
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

    public static final int SAMPLE_RATE = 48000;
    public static final int FRAME_SIZE = (SAMPLE_RATE / 1000) * 20;

    @Nullable
    private final String deviceName;
    private ALCdevice device;
    private ALCcontext context;

    @Deprecated
    public SoundManager(@Nullable String deviceName) throws SpeakerException {
        this.deviceName = deviceName;

        device = openSpeaker(deviceName);
        context = ALC10.alcCreateContext(device, (IntBuffer) null);

        PluginManager.instance().onCreateALContext(getContextAddress(context), getDeviceAddress(device));
    }

    public void close() {
        PluginManager.instance().onDestroyALContext(getContextAddress(context), getDeviceAddress(device));
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

    public boolean isClosed() {
        return context == null || device == null;
    }

    private ALCdevice openSpeaker(@Nullable String name) throws SpeakerException {
        try {
            return tryOpenSpeaker(name);
        } catch (SpeakerException e) {
            if (name != null) {
                Voicechat.LOGGER.warn("Failed to open audio channel '{}', falling back to default", name);
            }
            try {
                return tryOpenSpeaker(getDefaultSpeaker());
            } catch (SpeakerException ex) {
                return tryOpenSpeaker(null);
            }
        }
    }

    private ALCdevice tryOpenSpeaker(@Nullable String string) throws SpeakerException {
        ALCdevice l = ALC10.alcOpenDevice(string);
        if (l == null) {
            throw new SpeakerException("Failed to open audio device: Audio device not found");
        }
        checkAlcError(device);
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

    private static String getAlError(int i) {
        switch (i) {
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
                return "Unknown error";
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
