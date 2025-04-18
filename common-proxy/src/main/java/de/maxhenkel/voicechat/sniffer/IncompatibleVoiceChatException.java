package de.maxhenkel.voicechat.sniffer;

public class IncompatibleVoiceChatException extends Exception{

    public IncompatibleVoiceChatException(String message) {
        super(message);
    }

    public IncompatibleVoiceChatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatibleVoiceChatException(Throwable cause) {
        super(cause);
    }

}
