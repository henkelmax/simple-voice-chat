package de.maxhenkel.voicechat.api.events;

/**
 * This event is emitted on the client when the voice chat connects/disconnects.
 */
public interface ClientVoicechatConnectionEvent extends ClientEvent {

    /**
     * @return if the voice chat is connected to the server
     */
    boolean isConnected();

}
