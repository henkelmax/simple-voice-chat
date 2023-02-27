package de.maxhenkel.voicechat.api;

import java.net.SocketAddress;

/**
 * A socket used for server side voice chat traffic.
 * Can be set using {@link de.maxhenkel.voicechat.api.events.VoicechatServerStartingEvent#setSocketImplementation(VoicechatSocket)}.
 */
public interface VoicechatSocket {

    void open(int port, String bindAddress) throws Exception;

    RawUdpPacket read() throws Exception;

    void send(byte[] data, SocketAddress address) throws Exception;

    int getLocalPort();

    void close();

    boolean isClosed();

}
