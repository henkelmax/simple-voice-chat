package de.maxhenkel.voicechat.voice.client.microphone;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.MicrophoneException;
import de.maxhenkel.voicechat.voice.client.SoundManager;

import java.util.List;

public class MicrophoneManager {

    private static boolean fallback;

    public static Microphone getMicrophone() throws MicrophoneException {
        Microphone mic;
        if (fallback || VoicechatClient.CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            mic = getJavaMicrophone();
        } else {
            try {
                mic = getALMicrophone();
            } catch (MicrophoneException e) {
                Voicechat.LOGGER.warn("Failed to use OpenAL microphone implementation: {}", e.getMessage());
                Voicechat.LOGGER.warn("Falling back to Java microphone implementation");
                mic = getJavaMicrophone();
                fallback = true;
            }
        }
        return mic;
    }

    private static Microphone getJavaMicrophone() throws MicrophoneException {
        Microphone mic = new JavaxMicrophone(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, VoicechatClient.CLIENT_CONFIG.microphone.get());
        mic.open();
        return mic;
    }

    private static Microphone getALMicrophone() throws MicrophoneException {
        Microphone mic = new ALMicrophone(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, VoicechatClient.CLIENT_CONFIG.microphone.get());
        mic.open();
        return mic;
    }

    public static List<String> deviceNames() {
        if (fallback || VoicechatClient.CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            return JavaxMicrophone.getAllMicrophones();
        } else {
            return ALMicrophone.getAllMicrophones();
        }
    }

}
