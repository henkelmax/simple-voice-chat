package de.maxhenkel.voicechat.voice.client.microphone;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.client.MicrophoneException;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import org.lwjgl.openal.*;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ALMicrophone implements Microphone {

    private final int sampleRate;
    @Nullable
    private final String deviceName;
    private long device;
    private final int bufferSize;
    private boolean started;

    public ALMicrophone(int sampleRate, int bufferSize, @Nullable String deviceName) {
        this.sampleRate = sampleRate;
        this.deviceName = deviceName;
        this.bufferSize = bufferSize;
    }

    @Override
    public void open() throws MicrophoneException {
        if (isOpen()) {
            throw new MicrophoneException("Microphone already open");
        }
        if (!canCapture()) {
            throw new MicrophoneException("Extension 'ALC_EXT_CAPTURE' not supported");
        }
        device = openMic(deviceName);
    }

    @Override
    public void start() {
        if (!isOpen()) {
            return;
        }
        if (started) {
            return;
        }
        ALC11.alcCaptureStart(device);
        SoundManager.checkAlcError(device);
        started = true;
    }

    /**
     * Stops reading data from the microphone
     * Flushes all recorded data
     */
    @Override
    public void stop() {
        if (!isOpen()) {
            return;
        }
        if (!started) {
            return;
        }
        ALC11.alcCaptureStop(device);
        SoundManager.checkAlcError(device);
        started = false;

        int available = available();
        float[] data = new float[available];
        ALC11.alcCaptureSamples(device, data, data.length);
        SoundManager.checkAlcError(device);
        Voicechat.LOGGER.debug("Clearing {} samples", available);
    }

    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }
        stop();
        if (!ALC11.alcCaptureCloseDevice(device)) {
            SoundManager.checkAlcError(device);
        }
        device = 0;
    }

    @Override
    public boolean isOpen() {
        return device != 0;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public int available() {
        int samples = ALC11.alcGetInteger(device, ALC11.ALC_CAPTURE_SAMPLES);
        SoundManager.checkAlcError(device);
        return samples;
    }

    @Override
    public short[] read() {
        int available = available();
        if (bufferSize > available) {
            throw new IllegalStateException(String.format("Failed to read from microphone: Capacity %s, available %s", bufferSize, available));
        }
        float[] buff = new float[bufferSize];
        ALC11.alcCaptureSamples(device, buff, buff.length);
        SoundManager.checkAlcError(device);

        return AudioUtils.floatsToShortsNormalized(buff);
    }

    private long openMic(@Nullable String name) throws MicrophoneException {
        try {
            return tryOpenMic(name);
        } catch (MicrophoneException e) {
            if (name == null) {
                throw e;
            }
            Voicechat.LOGGER.warn("Failed to open microphone '{}', falling back to default microphone", name);
            return tryOpenMic(null);
        }
    }

    private long tryOpenMic(@Nullable String string) throws MicrophoneException {
        long device = ALC11.alcCaptureOpenDevice(string, sampleRate, EXTFloat32.AL_FORMAT_MONO_FLOAT32, bufferSize);
        if (device == 0L) {
            throw new MicrophoneException("Failed to open microphone");
        }
        SoundManager.checkAlcError(device);
        return device;
    }

    public static List<String> getAllMicrophones() {
        if (!canCapture()) {
            Voicechat.LOGGER.warn("Extension ALC_EXT_CAPTURE is not present");
            return Collections.emptyList();
        }
        if (!canEnumerate()) {
            Voicechat.LOGGER.warn("Extension ALC_ENUMERATION_EXT is not present");
            return Collections.emptyList();
        }
        List<String> devices = ALUtil.getStringList(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        if (devices == null) {
            Voicechat.LOGGER.warn("Failed to list available microphones");
            return Collections.emptyList();
        }
        return devices;
    }

    public static boolean canCapture() {
        return ALC11.alcIsExtensionPresent(0L, "ALC_EXT_CAPTURE");
    }

    public static boolean canEnumerate() {
        return ALC11.alcIsExtensionPresent(0L, "ALC_ENUMERATION_EXT");
    }
}
