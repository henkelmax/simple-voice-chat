package de.maxhenkel.voicechat.api;

import java.net.SocketAddress;

public interface VoicechatSocket {

    void open(int port, String bindAddress) throws Exception;

    RawUdpPacket read() throws Exception;

    void send(byte[] data, SocketAddress address) throws Exception;

    int getLocalPort();

    void close();

    boolean isClosed();

}
