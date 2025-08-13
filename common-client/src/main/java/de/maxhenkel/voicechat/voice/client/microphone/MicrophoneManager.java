package de.maxhenkel.voicechat.voice.client.microphone;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.MicrophoneException;
import de.maxhenkel.voicechat.voice.common.AudioUtils;

import java.util.List;

public class MicrophoneManager {

    private static boolean fallback;

    public static Microphone createMicrophone() throws MicrophoneException {
        Microphone mic;
        if (fallback || VoicechatClient.CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            mic = createJavaMicrophone();
        } else {
            try {
                mic = createALMicrophone();
            } catch (MicrophoneException e) {
                Voicechat.LOGGER.warn("Failed to use OpenAL microphone implementation", e);
                Voicechat.LOGGER.warn("Falling back to Java microphone implementation");
                mic = createJavaMicrophone();
                fallback = true;
            }
        }
        return mic;
    }

    private static Microphone createJavaMicrophone() throws MicrophoneException {
        Microphone mic = new JavaxMicrophone(AudioUtils.SAMPLE_RATE, AudioUtils.FRAME_SIZE, VoicechatClient.CLIENT_CONFIG.microphone.get());
        mic.open();
        return mic;
    }

    private static Microphone createALMicrophone() throws MicrophoneException {
        Microphone mic = new ALMicrophone(AudioUtils.SAMPLE_RATE, AudioUtils.FRAME_SIZE, VoicechatClient.CLIENT_CONFIG.microphone.get());
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
