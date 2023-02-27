package de.maxhenkel.voicechat.api;

import java.net.SocketAddress;

/**
 * A socket used for client side voice chat traffic.
 * Can be set using {@link de.maxhenkel.voicechat.api.events.ClientVoicechatInitializationEvent#setSocketImplementation(ClientVoicechatSocket)}.
 */
public interface ClientVoicechatSocket {

    void open() throws Exception;

    RawUdpPacket read() throws Exception;

    void send(byte[] data, SocketAddress address) throws Exception;

    void close();

    boolean isClosed();

}
