package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.RawUdpPacket;

import java.net.SocketAddress;

public class RawUdpPacketImpl implements RawUdpPacket {

    private static final byte[] BUFFER = new byte[4096];

    private final byte[] data;
    private final SocketAddress socketAddress;
    private final long timestamp;

    public RawUdpPacketImpl(byte[] data, SocketAddress socketAddress, long timestamp) {
        this.data = data;
        this.socketAddress = socketAddress;
        this.timestamp = timestamp;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }
}
