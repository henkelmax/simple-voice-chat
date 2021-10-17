package de.maxhenkel.voicechat.voice.client.microphone;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.client.MicrophoneException;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class JavaxMicrophone implements Microphone {

    private final int sampleRate;
    @Nullable
    private final String deviceName;
    private final int bufferSize;
    @Nullable
    private TargetDataLine mic;

    public JavaxMicrophone(int sampleRate, int bufferSize, @Nullable String deviceName) {
        this.sampleRate = sampleRate;
        this.deviceName = deviceName;
        this.bufferSize = bufferSize;
    }

    @Override
    public void open() throws MicrophoneException {
        if (isOpen()) {
            throw new MicrophoneException("Microphone already open");
        }

        AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);

        mic = getMicrophoneByName(af, deviceName);

        if (mic == null) {
            if (deviceName != null) {
                Voicechat.LOGGER.warn("Failed to open microphone '{}', falling back to default microphone", deviceName);
            }
            mic = getDefaultMicrophone(af);
        }

        if (mic == null) {
            throw new MicrophoneException("Could not find any microphone with the specified audio format");
        }
        try {
            mic.open(af);
        } catch (LineUnavailableException e) {
            throw new MicrophoneException(e.getMessage());
        }

        // This fixes the accumulating audio issue on some Linux systems
        mic.start();
        mic.stop();
        mic.flush();
    }

    @Override
    public void start() {
        if (!isOpen() || mic == null) {
            return;
        }
        mic.start();
    }

    @Override
    public void stop() {
        if (!isOpen() || mic == null) {
            return;
        }
        mic.stop();
        mic.flush();
    }

    @Override
    public void close() {
        if (mic == null) {
            return;
        }
        mic.stop();
        mic.flush();
        mic.close();
    }

    @Override
    public boolean isOpen() {
        if (mic == null) {
            return false;
        }
        return mic.isOpen();
    }

    @Override
    public boolean isStarted() {
        if (mic == null) {
            return false;
        }
        return mic.isActive();
    }

    @Override
    public int available() {
        if (mic == null) {
            return 0;
        }
        return mic.available() / 2;
    }

    @Override
    public short[] read() {
        if (mic == null) {
            throw new IllegalStateException("Microphone was not opened");
        }
        int available = available();
        if (bufferSize > available) {
            throw new IllegalStateException(String.format("Failed to read from microphone: Capacity %s, available %s", bufferSize, available));
        }
        byte[] buff = new byte[bufferSize * 2];
        mic.read(buff, 0, buff.length);
        return Utils.bytesToShorts(buff);
    }

    @Nullable
    private static TargetDataLine getDefaultMicrophone(AudioFormat format) {
        return getDefaultDevice(TargetDataLine.class, format);
    }

    @Nullable
    private static <T> T getDefaultDevice(Class<T> lineClass, AudioFormat format) {
        DataLine.Info info = new DataLine.Info(lineClass, format);
        try {
            return lineClass.cast(AudioSystem.getLine(info));
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static TargetDataLine getMicrophoneByName(AudioFormat format, @Nullable String name) {
        return getDeviceByName(TargetDataLine.class, format, name);
    }

    @Nullable
    private static <T> T getDeviceByName(Class<T> lineClass, AudioFormat format, @Nullable String name) {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            DataLine.Info lineInfo = new DataLine.Info(lineClass, format);
            if (mixer.isLineSupported(lineInfo)) {
                if (mixerInfo.getName().equals(name)) {
                    try {
                        return lineClass.cast(mixer.getLine(lineInfo));
                    } catch (Exception e) {
                    }
                }
            }
        }
        return null;
    }

    public static List<String> getAllMicrophones() {
        return getAllMicrophones(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SoundManager.SAMPLE_RATE, 16, 1, 2, SoundManager.SAMPLE_RATE, false));
    }

    private static List<String> getAllMicrophones(AudioFormat format) {
        return getDeviceNames(TargetDataLine.class, format);
    }

    private static List<String> getDeviceNames(Class<?> lineClass, AudioFormat format) {
        List<String> names = new ArrayList<>();
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            DataLine.Info lineInfo = new DataLine.Info(lineClass, format);
            if (mixer.isLineSupported(lineInfo)) {
                names.add(mixerInfo.getName());
            }
        }
        return names;
    }

}
