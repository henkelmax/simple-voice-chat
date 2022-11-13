package de.maxhenkel.voicechat.voice.client.microphone;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.client.MicrophoneException;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;

public class ALMicrophone implements Microphone {

    private final int sampleRate;
    @Nullable
    private final String deviceName;
    private ALCdevice device;
    private final int bufferSize;
    private boolean started;
    private final ByteBuffer buffer;

    public ALMicrophone(int sampleRate, int bufferSize, @Nullable String deviceName) {
        this.sampleRate = sampleRate;
        this.deviceName = deviceName;
        this.bufferSize = bufferSize;
        this.buffer = BufferUtils.createByteBuffer(bufferSize * 2);
    }

    @Override
    public void open() throws MicrophoneException {
        if (isOpen()) {
            throw new MicrophoneException("Microphone already open");
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

        buffer.reset();
        ALC11.alcCaptureSamples(device, buffer, available);
        SoundManager.checkAlcError(device);
        Voicechat.LOGGER.debug("Clearing {} samples", available);
    }

    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }
        stop();
        ALC11.alcCaptureCloseDevice(device);
        SoundManager.checkAlcError(device);
        device = null;
    }

    @Override
    public boolean isOpen() {
        return device != null;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public int available() {
        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        ALC10.alcGetInteger(device, ALC11.ALC_CAPTURE_SAMPLES, buffer);
        SoundManager.checkAlcError(device);
        return buffer.get();
    }

    @Override
    public short[] read() {
        int available = available();
        if (bufferSize > available) {
            throw new IllegalStateException(String.format("Failed to read from microphone: Capacity %s, available %s", bufferSize, available));
        }
        short[] buff = new short[bufferSize];
        buffer.clear();
        ALC11.alcCaptureSamples(device, buffer, buff.length);
        SoundManager.checkAlcError(device);
        buffer.rewind();
        buffer.asShortBuffer().get(buff); //TODO Fix crackling mic audio
        return buff;
    }

    private ALCdevice openMic(@Nullable String name) throws MicrophoneException {
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

    private ALCdevice tryOpenMic(@Nullable String string) throws MicrophoneException {
        ALCdevice l = ALC11.alcCaptureOpenDevice(string, sampleRate, AL10.AL_FORMAT_MONO16, bufferSize);
        if (l == null) {
            SoundManager.checkAlcError(null);
            throw new MicrophoneException(String.format("Failed to open microphone: %s", SoundManager.getAlcError(0)));
        }
        SoundManager.checkAlcError(l);
        return l;
    }

    @Nullable
    public static String getDefaultMicrophone() {
        if (!SoundManager.canEnumerate()) {
            return null;
        }
        String mic = ALC10.alcGetString(null, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        SoundManager.checkAlcError(null);
        return mic;
    }

    public static List<String> getAllMicrophones() {
        if (!SoundManager.canEnumerate()) {
            return Collections.emptyList();
        }
        // TODO Fix getting all microphones
        // List<String> devices = ALUtil.getStringList(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        // SoundManager.checkAlcError(0L);
        // return devices == null ? Collections.emptyList() : devices;
        return Collections.emptyList();
    }
}
