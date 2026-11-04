package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.RawUdpPacket;

import java.net.SocketAddress;

public class RawUdpPacketImpl implements RawUdpPacket {

    private final byte[] data;
    private final SocketAddress socketAddress;

    public RawUdpPacketImpl(byte[] data, SocketAddress socketAddress) {
        this.data = data;
        this.socketAddress = socketAddress;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public long getTimestamp() {
        return 0L;
    }

    @Override
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }
}
