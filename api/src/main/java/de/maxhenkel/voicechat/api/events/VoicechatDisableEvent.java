package de.maxhenkel.voicechat.api.events;

/**
 * This event is emitted on the client when the voice chat is getting enabled/disabled.
 */
public interface VoicechatDisableEvent extends ClientEvent {

    /**
     * @return if the voice chat is disabled
     */
    boolean isDisabled();

}
