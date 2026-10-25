package de.maxhenkel.voicechat.api;

import java.net.SocketAddress;

/**
 * A socket used for client side voice chat traffic.
 * Can be set using {@link de.maxhenkel.voicechat.api.events.ClientVoicechatInitializationEvent#setSocketImplementation(ClientVoicechatSocket)}.
 */
public interface ClientVoicechatSocket {

    void open() throws Exception;

    /**
     * Blocks until a packet is received.
     *
     * @return the received packet
     * @throws java.net.ProtocolException if the received packet is invalid and should be skipped
     * @throws Exception                  if the socket can't be read from - This disconnects the client
     */
    RawUdpPacket read() throws Exception;

    void send(byte[] data, SocketAddress address) throws Exception;

    void close();

    boolean isClosed();

}
