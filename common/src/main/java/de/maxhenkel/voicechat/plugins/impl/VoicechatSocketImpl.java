package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.api.VoicechatSocket;

import java.net.*;

public class VoicechatSocketImpl implements VoicechatSocket {

    private DatagramSocket socket;

    @Override
    public void open(int port, String bindAddress) throws Exception {
        if (socket != null) {
            throw new IllegalStateException("Socket already opened");
        }
        checkCorrectHost();
        InetAddress address = null;
        try {
            if (!bindAddress.isEmpty()) {
                address = InetAddress.getByName(bindAddress);
            }
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to parse bind IP address '{}'", bindAddress);
            e.printStackTrace();
            Voicechat.LOGGER.info("Binding to default IP address");
        }

        try {
            try {
                socket = new DatagramSocket(port, address);
            } catch (BindException e) {
                if (address == null || bindAddress.equals("0.0.0.0")) {
                    throw e;
                }
                Voicechat.LOGGER.fatal("Failed to bind to address '{}', binding to '0.0.0.0' instead", bindAddress);
                socket = new DatagramSocket(port);
            }
            socket.setTrafficClass(0x04); // IPTOS_RELIABILITY
        } catch (BindException e) {
            Voicechat.LOGGER.error("Failed to bind to address '0.0.0.0', make sure no other application is running at UDP port {}", port);
            e.printStackTrace();
            System.exit(1);
            return;
        }
    }

    private void checkCorrectHost() {
        String host = Voicechat.SERVER_CONFIG.voiceHost.get();
        if (!host.isEmpty()) {
            try {
                new URI("voicechat://" + host);
            } catch (URISyntaxException e) {
                Voicechat.LOGGER.warn("Failed to parse voice host: {}", e.getMessage());
                System.exit(1);
            }
        }
    }

    @Override
    public RawUdpPacket read() throws Exception {
        return RawUdpPacketImpl.read(socket);
    }

    @Override
    public void send(byte[] data, SocketAddress address) throws Exception {
        socket.send(new DatagramPacket(data, data.length, address));
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }
}
