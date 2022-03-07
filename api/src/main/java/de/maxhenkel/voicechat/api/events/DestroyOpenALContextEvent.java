package de.maxhenkel.voicechat.api.events;

/**
 * This event is emitted when the voice chats OpenAL context is destroyed.
 */
public interface DestroyOpenALContextEvent extends ClientEvent {

    /**
     * @return the OpenAL context - Might be <code>0L</code> in some cases
     */
    long getContext();

    /**
     * @return the OpenAL device - Might be <code>0L</code> in some cases
     */
    long getDevice();

}
