package de.maxhenkel.voicechat.voice.client.microphone;

import de.maxhenkel.voicechat.voice.client.MicrophoneException;

import javax.annotation.Nullable;

public interface Microphone {

    void open() throws MicrophoneException;

    void start();

    void stop();

    void close();

    boolean isOpen();

    boolean isStarted();

    /**
     * @return the name of the opened device or <code>null</code> if it is unknown
     */
    @Nullable
    String getDeviceName();

    boolean isDisconnected();

    int available();

    short[] read();

}
