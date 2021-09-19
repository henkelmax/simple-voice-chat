package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.Utils;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;

import javax.annotation.Nullable;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ALMicrophone {

    private final int bytesPerFrame = 2;
    private final int sampleRate;
    private final int bufferSize;
    private final String deviceName;
    private long device;
    private final short[] buffer;
    private final IntBuffer sampleCount;

    public ALMicrophone(int sampleRate, int bufferSizeBytes, String deviceName) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSizeBytes;
        this.deviceName = deviceName;
        this.device = -1L;
        this.buffer = new short[bufferSize / 2];
        this.sampleCount = BufferUtils.createIntBuffer(1);
    }

    public void open() throws MicrophoneException {
        if (isOpen()) {
            throw new MicrophoneException("Microphone already open");
        }
        device = openMic(deviceName);
    }

    public void start() {
        if (!isOpen()) {
            return;
        }
        ALC11.alcCaptureStart(device);
    }

    /**
     * Stops reading data from the microphone
     * Flushes all recorded data
     */
    public void stop() {
        if (!isOpen()) {
            return;
        }
        ALC11.alcCaptureStop(device);
    }

    public void close() {
        if (!isOpen()) {
            return;
        }
        ALC11.alcCaptureCloseDevice(device);
        device = -1;
    }

    public boolean isOpen() {
        return device >= 0;
    }

    /**
     * @return the amount of bytes available
     */
    public int available() {
        ALC11.alcGetIntegerv(device, ALC11.ALC_CAPTURE_SAMPLES, sampleCount);
        int samples = sampleCount.get(0);
        return samples * bytesPerFrame;
    }

    public byte[] read(byte[] data) {
        if (data.length != buffer.length * bytesPerFrame) {
            throw new IllegalArgumentException(String.format("Failed to read from microphone: Read size %s, capacity %s", data.length, buffer.length * bytesPerFrame));
        }
        int available = available();
        if (buffer.length * bytesPerFrame > available) {
            throw new IllegalStateException(String.format("Failed to read from microphone: Capacity %s, available %s", buffer.length * bytesPerFrame, available));
        }
        ALC11.alcCaptureSamples(device, buffer, buffer.length);
        for (int i = 0; i < buffer.length; i++) {
            byte[] bytes = Utils.shortToBytes(buffer[i]);
            data[i * 2] = bytes[0];
            data[i * 2 + 1] = bytes[1];
        }
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
            throw new MicrophoneException(String.format("Failed to open microphone: %s", getError(0)));
        }
        int err = ALC11.alcGetError(l);
        if (err != 0) {
            throw new MicrophoneException(String.format("Failed to open microphone: %s", getError(err)));
        }
        return l;
    }

    private static String getError(int i) {
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

    @Nullable
    public static String getDefaultMicrophone() {
        if (!canEnumerate()) {
            return null;
        }
        return ALC11.alcGetString(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
    }

    public static List<String> getAllMicrophones() {
        if (!canEnumerate()) {
            return Collections.emptyList();
        }
        List<String> devices = ALUtil.getStringList(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        return devices == null ? Collections.emptyList() : devices;
    }

    private static final Pattern DEVICE_NAME = Pattern.compile("^(?:OpenAL.+on )?(.*)$");

    public static String cleanDeviceName(String name) {
        Matcher matcher = DEVICE_NAME.matcher(name);
        if (!matcher.matches()) {
            return name;
        }
        return matcher.group(1);
    }

    private static boolean canEnumerate() {
        return ALC11.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT");
    }

}
