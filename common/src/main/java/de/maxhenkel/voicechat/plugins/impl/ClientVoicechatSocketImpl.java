package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ClientVoicechatSocket;
import de.maxhenkel.voicechat.api.RawUdpPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class ClientVoicechatSocketImpl implements ClientVoicechatSocket {

    private DatagramSocket socket;

    @Override
    public void open() throws Exception {
        this.socket = new DatagramSocket();
    }

    @Override
    public RawUdpPacket read() throws Exception {
        if (socket == null) {
            throw new IllegalStateException("Socket not opened yet");
        }
        return RawUdpPacketImpl.read(socket);
    }

    @Override
    public void send(byte[] data, SocketAddress address) throws Exception {
        if (socket == null) {
            return; // Ignoring packet sending when socket isn't open yet
        }
        socket.send(new DatagramPacket(data, data.length, address));
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

    @Override
    public boolean isClosed() {
        return socket == null;
    }
}
