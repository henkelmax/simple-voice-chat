package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.ClientVoicechatSocket;

import javax.annotation.Nullable;

public interface ClientVoicechatInitializationEvent extends ClientEvent {

    /**
     * Sets a custom implementation of the socket used for client side voice chat traffic.
     *
     * @param socket the custom socket implementation
     */
    void setSocketImplementation(ClientVoicechatSocket socket);

    /**
     * @return the custom socket implementation or <code>null</code> to use voice chats default one
     */
    @Nullable
    ClientVoicechatSocket getSocketImplementation();

}
