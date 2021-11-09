package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.RawUdpPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class RawUdpPacketImpl implements RawUdpPacket {

    private final DatagramPacket packet;
    private final long timestamp;

    public RawUdpPacketImpl(DatagramPacket packet, long timestamp) {
        this.packet = packet;
        this.timestamp = timestamp;
    }

    public static RawUdpPacketImpl read(DatagramSocket socket) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
        socket.receive(packet);
        // Setting the timestamp after receiving the packet
        long timestamp = System.currentTimeMillis();
        return new RawUdpPacketImpl(packet, timestamp);
    }

    @Override
    public byte[] getData() {
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
        return data;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public SocketAddress getSocketAddress() {
        return packet.getSocketAddress();
    }
}
