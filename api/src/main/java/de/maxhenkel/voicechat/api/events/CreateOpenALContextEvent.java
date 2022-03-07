package de.maxhenkel.voicechat.api.events;

/**
 * This event is emitted when the voice chats OpenAL context is created.
 */
public interface CreateOpenALContextEvent extends ClientEvent {

    /**
     * @return the OpenAL context
     */
    long getContext();

    /**
     * @return the OpenAL device
     */
    long getDevice();

}
