package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.RawUdpPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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

    public static RawUdpPacketImpl read(DatagramSocket socket) throws IOException {
        synchronized (BUFFER) {
            DatagramPacket packet = new DatagramPacket(BUFFER, BUFFER.length);
            socket.receive(packet);
            // Setting the timestamp after receiving the packet
            long timestamp = System.currentTimeMillis();
            byte[] data = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
            return new RawUdpPacketImpl(data, packet.getSocketAddress(), timestamp);
        }
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
