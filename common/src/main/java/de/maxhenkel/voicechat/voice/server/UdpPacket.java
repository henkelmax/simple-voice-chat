package de.maxhenkel.voicechat.voice.server;

import java.net.SocketAddress;

public class UdpPacket {

    private final byte[] data;
    private final SocketAddress socketAddress;
    private final long timestamp;

    public UdpPacket(byte[] data, SocketAddress socketAddress, long timestamp) {
        this.data = data;
        this.socketAddress = socketAddress;
        this.timestamp = timestamp;
    }

    public byte[] getData() {
        return data;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
