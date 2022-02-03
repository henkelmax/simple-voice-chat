package de.maxhenkel.voicechat.api.events;

/**
 * This event is emitted on the client when the state of the microphone changes.
 */
public interface MicrophoneMuteEvent extends ClientEvent {

    /**
     * @return if the microphone is muted
     */
    boolean isDisabled();

}
