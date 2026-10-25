package de.maxhenkel.voicechat.api;

import java.net.SocketAddress;

/**
 * A socket used for server side voice chat traffic.
 * Can be set using {@link de.maxhenkel.voicechat.api.events.VoicechatServerStartingEvent#setSocketImplementation(VoicechatSocket)}.
 */
public interface VoicechatSocket {

    void open(int port, String bindAddress) throws Exception;

    /**
     * Blocks until a packet is received.
     *
     * @return the received packet
     * @throws java.net.ProtocolException if the received packet is invalid and should be skipped
     * @throws Exception                  if the socket can't be read from - Reading is retried as long as the socket isn't closed
     */
    RawUdpPacket read() throws Exception;

    void send(byte[] data, SocketAddress address) throws Exception;

    int getLocalPort();

    void close();

    boolean isClosed();

}
