package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ALMicrophone {

    private final int sampleRate;
    private final String deviceName;
    private long device;
    private final int bufferSize;
    @Nullable
    private final MicrophoneListener listener;
    private boolean started;

    public ALMicrophone(int sampleRate, int bufferSize, String deviceName) {
        this(sampleRate, bufferSize, deviceName, null);
    }

    public ALMicrophone(int sampleRate, int bufferSize, String deviceName, @Nullable MicrophoneListener listener) {
        this.sampleRate = sampleRate;
        this.deviceName = deviceName;
        this.bufferSize = bufferSize;
        this.listener = listener;
    }

    public void open() throws MicrophoneException {
        if (isOpen()) {
            throw new MicrophoneException("Microphone already open");
        }
        if (listener != null) {
            listener.onOpen();
        }
        device = openMic(deviceName);
    }

    public void start() {
        if (!isOpen()) {
            return;
        }
        if (started) {
            return;
        }
        if (listener != null) {
            listener.onStart();
        }
        ALC11.alcCaptureStart(device);
        SoundManager.checkAlError();
        started = true;
    }

    /**
     * Stops reading data from the microphone
     * Flushes all recorded data
     */
    public void stop() {
        if (!isOpen()) {
            return;
        }
        if (!started) {
            return;
        }
        if (listener != null) {
            listener.onStop();
        }
        ALC11.alcCaptureStop(device);
        SoundManager.checkAlError();
        started = false;

        int available = available();
        short[] data = new short[available];
        ALC11.alcCaptureSamples(device, data, data.length);
        SoundManager.checkAlError();
        Voicechat.LOGGER.debug("Clearing {} samples", available);
    }

    public void close() {
        if (!isOpen()) {
            return;
        }
        if (listener != null) {
            listener.onClose();
        }
        stop();
        ALC11.alcCaptureCloseDevice(device);
        SoundManager.checkAlError();
        device = 0;
    }

    public boolean isOpen() {
        return device != 0;
    }

    public boolean isStarted() {
        return started;
    }

    public int available() {
        int samples = ALC11.alcGetInteger(device, ALC11.ALC_CAPTURE_SAMPLES);
        SoundManager.checkAlError();
        return samples;
    }

    public short[] read(short[] data) {
        int available = available();
        if (data.length > available) {
            throw new IllegalStateException(String.format("Failed to read from microphone: Capacity %s, available %s", data.length, available));
        }
        ALC11.alcCaptureSamples(device, data, data.length);
        SoundManager.checkAlError();
        return data;
    }

    private long openMic(@Nullable String name) throws MicrophoneException {
        try {
            return tryOpenMic(name);
        } catch (MicrophoneException e) {
            if (name != null) {
                Voicechat.LOGGER.warn("Failed to open microphone '{}', falling back to default microphone", name);
            }
            try {
                return tryOpenMic(getDefaultMicrophone());
            } catch (MicrophoneException ex) {
                return tryOpenMic(null);
            }
        }
    }

    private long tryOpenMic(@Nullable String string) throws MicrophoneException {
        long l = ALC11.alcCaptureOpenDevice(string, sampleRate, AL11.AL_FORMAT_MONO16, bufferSize);
        if (l == 0L) {
            throw new MicrophoneException(String.format("Failed to open microphone: %s", SoundManager.getError(0)));
        }
        SoundManager.checkAlError();
        return l;
    }

    @Nullable
    public static String getDefaultMicrophone() {
        if (!SoundManager.canEnumerate()) {
            return null;
        }
        String mic = ALC11.alcGetString(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        SoundManager.checkAlError();
        return mic;
    }

    public static List<String> getAllMicrophones() {
        if (!SoundManager.canEnumerate()) {
            return Collections.emptyList();
        }
        List<String> devices = ALUtil.getStringList(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        SoundManager.checkAlError();
        return devices == null ? Collections.emptyList() : devices;
    }

    public interface MicrophoneListener {
        default void onOpen() {
        }

        default void onStart() {
        }

        default void onStop() {
        }

        default void onClose() {
        }
    }
}
