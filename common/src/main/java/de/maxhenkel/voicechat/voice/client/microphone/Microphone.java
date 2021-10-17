package de.maxhenkel.voicechat.voice.client.microphone;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.MicrophoneException;

import java.util.List;

public interface Microphone {

    void open() throws MicrophoneException;

    void start();

    void stop();

    void close();

    boolean isOpen();

    boolean isStarted();

    int available();

    short[] read();

    public static List<String> deviceNames() {
        if (VoicechatClient.CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            return JavaxMicrophone.getAllMicrophones();
        } else {
            return ALMicrophone.getAllMicrophones();
        }
    }

}
