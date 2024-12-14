package de.maxhenkel.voicechat.voice.client.microphone;

import de.maxhenkel.voicechat.voice.client.MicrophoneException;

public interface Microphone {

    void open() throws MicrophoneException;

    void start();

    void stop();

    void close();

    boolean isOpen();

    boolean isStarted();

    int available();

    short[] read();

}
