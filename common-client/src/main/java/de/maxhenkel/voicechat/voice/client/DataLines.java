package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;

import javax.annotation.Nullable;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class DataLines {

    @Nullable
    public static TargetDataLine getMicrophone(AudioFormat format) {
        String micName = VoicechatClient.CLIENT_CONFIG.microphone.get();
        if (!micName.isEmpty()) {
            TargetDataLine mic = getMicrophoneByName(format, micName);
            if (mic != null) {
                return mic;
            }
        }
        return getDefaultMicrophone(format);
    }

    @Nullable
    public static SourceDataLine getSpeaker(AudioFormat format) {
        String speakerName = VoicechatClient.CLIENT_CONFIG.speaker.get();
        if (!speakerName.isEmpty()) {
            SourceDataLine speaker = getSpeakerByName(format, speakerName);
            if (speaker != null) {
                return speaker;
            }
        }
        return getDefaultSpeaker(format);
    }

    @Nullable
    public static TargetDataLine getDefaultMicrophone(AudioFormat format) {
        return getDefaultDevice(TargetDataLine.class, format);
    }

    @Nullable
    public static SourceDataLine getDefaultSpeaker(AudioFormat format) {
        return getDefaultDevice(SourceDataLine.class, format);
    }

    @Nullable
    public static <T> T getDefaultDevice(Class<T> lineClass, AudioFormat format) {
        DataLine.Info info = new DataLine.Info(lineClass, format);
        try {
            return lineClass.cast(AudioSystem.getLine(info));
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static TargetDataLine getMicrophoneByName(AudioFormat format, String name) {
        return getDeviceByName(TargetDataLine.class, format, name);
    }

    @Nullable
    public static SourceDataLine getSpeakerByName(AudioFormat format, String name) {
        return getDeviceByName(SourceDataLine.class, format, name);
    }

    @Nullable
    public static <T> T getDeviceByName(Class<T> lineClass, AudioFormat format, String name) {
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

    public static List<String> getMicrophoneNames(AudioFormat format) {
        return getDeviceNames(TargetDataLine.class, format);
    }

    public static List<String> getSpeakerNames(AudioFormat format) {
        return getDeviceNames(SourceDataLine.class, format);
    }

    public static List<String> getDeviceNames(Class<?> lineClass, AudioFormat format) {
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

