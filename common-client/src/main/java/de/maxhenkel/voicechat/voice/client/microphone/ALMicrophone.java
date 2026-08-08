package de.maxhenkel.voicechat.voice.client.microphone;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.Version;
import de.maxhenkel.voicechat.voice.client.MicrophoneException;
import de.maxhenkel.voicechat.voice.client.OpenALVersion;
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
    private boolean captureStereo;
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
        captureStereo = useStereoWorkaround();
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
        float[] data = new float[captureStereo ? available * 2 : available];
        ALC11.alcCaptureSamples(device, data, available);
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
        float[] buff = new float[captureStereo ? bufferSize * 2 : bufferSize];
        ALC11.alcCaptureSamples(device, buff, bufferSize);
        SoundManager.checkAlcError(device);

        if (captureStereo) {
            return AudioUtils.stereoFloatsToMonoShortsNormalized(buff);
        } else {
            return AudioUtils.floatsToShortsNormalized(buff);
        }
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
        long device = ALC11.alcCaptureOpenDevice(string, sampleRate, captureStereo ? EXTFloat32.AL_FORMAT_STEREO_FLOAT32 : EXTFloat32.AL_FORMAT_MONO_FLOAT32, bufferSize);
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

    private static Boolean alStereoWorkaround = null;

    private static boolean useStereoWorkaround() {
        if (alStereoWorkaround == null) {
            alStereoWorkaround = shouldUseStereoWorkaround();
            if (alStereoWorkaround) {
                Voicechat.LOGGER.info("Using stereo workaround for OpenAL microphones");
            }
        }
        return alStereoWorkaround;
    }

    private static boolean shouldUseStereoWorkaround() {
        Version alVersion = OpenALVersion.get();
        if (alVersion == null) {
            Voicechat.LOGGER.warn("Failed to get OpenAL version - assuming stereo workaround is required");
            return true;
        }
        // OpenAL 1.25 and 1.25.1 has a broken implementation of Multi2Mono which causes stereo microphones to crackle when downmixed to mono
        // This has been fixed by https://github.com/kcat/openal-soft/pull/1246 so it will work again in 1.25.2+
        return alVersion.compareTo(new Version(1, 25, 0)) >= 0 && alVersion.compareTo(new Version(1, 25, 1)) <= 0;
    }

}
