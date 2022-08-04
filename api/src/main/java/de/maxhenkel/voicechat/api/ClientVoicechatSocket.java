package de.maxhenkel.voicechat.api;

import java.net.SocketAddress;

public interface ClientVoicechatSocket {

    void open() throws Exception;

    RawUdpPacket read() throws Exception;

    void send(byte[] data, SocketAddress address) throws Exception;

    void close();

    boolean isClosed();

}
