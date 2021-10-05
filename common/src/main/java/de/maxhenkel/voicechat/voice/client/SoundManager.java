package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.openal.EXTThreadLocalContext;

import javax.annotation.Nullable;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SoundManager {

    public static final int SAMPLE_RATE = 48000;
    public static final int FRAME_SIZE = (SAMPLE_RATE / 1000) * 20;

    private final Minecraft mc;
    @Nullable
    private final String deviceName;
    private long device;
    private long context;

    public SoundManager(@Nullable String deviceName) throws SpeakerException {
        mc = Minecraft.getInstance();
        this.deviceName = deviceName;

        device = openSpeaker(deviceName);
        context = ALC11.alcCreateContext(device, (IntBuffer) null);
    }

    public void close() {
        if (context != 0L) {
            ALC11.alcDestroyContext(context);
            checkAlError();
        }
        if (device != 0L) {
            ALC11.alcCloseDevice(device);
            checkAlError();
        }
        context = 0;
        device = 0;
    }

    public boolean isClosed() {
        return context == 0 || device == 0;
    }

    private long openSpeaker(@Nullable String name) throws SpeakerException {
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

    private long tryOpenSpeaker(@Nullable String string) throws SpeakerException {
        long l = ALC11.alcOpenDevice(string);
        if (l == 0L) {
            throw new SpeakerException(String.format("Failed to open audio device: %s", getError(0)));
        }
        SoundManager.checkAlError();
        return l;
    }

    @Nullable
    public static String getDefaultSpeaker() {
        if (!canEnumerate()) {
            return null;
        }
        String defaultSpeaker = ALC11.alcGetString(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER);
        checkAlError();
        return defaultSpeaker;
    }

    public static List<String> getAllSpeakers() {
        if (!canEnumerate()) {
            return Collections.emptyList();
        }
        List<String> devices = ALUtil.getStringList(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER);
        checkAlError();
        return devices == null ? Collections.emptyList() : devices;
    }

    public void runInContext(Executor executor, Runnable runnable) {
        long time = System.currentTimeMillis();
        executor.execute(() -> {
            long diff = System.currentTimeMillis() - time;
            if (diff > 20 || (diff >= 5 && CommonCompatibilityManager.INSTANCE.isDevEnvironment())) {
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
        checkAlError();
        return success;
    }

    public void closeContext() {
        EXTThreadLocalContext.alcSetThreadContext(0L);
        checkAlError();
    }

    public static boolean checkAlError() {
        int error = AL11.alGetError();
        if (error == AL11.AL_NO_ERROR) {
            return false;
        }
        StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
        Voicechat.LOGGER.error("Voicechat sound manager error: {}.{}[{}] {}", stack.getClassName(), stack.getMethodName(), stack.getLineNumber(), getError(error));
        return true;
    }

    public static String getError(int i) {
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

    private static final Pattern DEVICE_NAME = Pattern.compile("^(?:OpenAL.+on )?(.*)$");

    public static String cleanDeviceName(String name) {
        Matcher matcher = DEVICE_NAME.matcher(name);
        if (!matcher.matches()) {
            return name;
        }
        return matcher.group(1);
    }

    public static boolean canEnumerate() {
        boolean present = ALC11.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT");
        SoundManager.checkAlError();
        return present;
    }

}
