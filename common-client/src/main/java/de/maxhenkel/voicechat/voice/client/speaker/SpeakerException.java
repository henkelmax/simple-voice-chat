package de.maxhenkel.voicechat.voice.client.speaker;

import java.io.IOException;

public class SpeakerException extends IOException {

    public SpeakerException(String message) {
        super(message);
    }

    public SpeakerException(String message, Throwable cause) {
        super(message, cause);
    }

}
