package de.maxhenkel.voicechat.voice.client.microphone;

import com.sun.jna.Platform;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.util.Version;
import de.maxhenkel.voicechat.voice.client.MicrophoneException;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import org.lwjgl.openal.AL11;

import java.util.List;

public class MicrophoneManager {

    private static boolean fallback;

    public static Microphone createMicrophone() throws MicrophoneException {
        Microphone mic;
        if (useJavaImplementation()) {
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
        if (useJavaImplementation()) {
            return JavaxMicrophone.getAllMicrophones();
        } else {
            return ALMicrophone.getAllMicrophones();
        }
    }

    private static Boolean forceJavaImplementation = null;

    private static boolean shouldForceJavaImplementation() {
        if (forceJavaImplementation == null) {
            forceJavaImplementation = !canUseOpenAL();
            if (forceJavaImplementation) {
                Voicechat.LOGGER.info("OpenAL microphones are not properly supported on this platform, falling back to Java microphone implementation");
            }
        }
        return forceJavaImplementation;
    }

    public static boolean canUseOpenAL() {
        // OpenAL is completely broken on macOS
        if (Platform.isMac()) {
            return false;
        }
        return true;
    }

    public static boolean useJavaImplementation() {
        if (shouldForceJavaImplementation()) {
            return true;
        }
        return fallback || VoicechatClient.CLIENT_CONFIG.javaMicrophoneImplementation.get();
    }

}
