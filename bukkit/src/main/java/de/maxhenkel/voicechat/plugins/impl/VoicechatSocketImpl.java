package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.api.VoicechatSocket;
import org.bukkit.Bukkit;

import java.net.*;

public class VoicechatSocketImpl extends VoicechatSocketBase implements VoicechatSocket {

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
            Voicechat.LOGGER.info("Binding to wildcard IP address");
        }

        try {
            try {
                socket = new DatagramSocket(port, address);
            } catch (BindException e) {
                if (address == null || bindAddress.equals("0.0.0.0")) {
                    throw e;
                }
                Voicechat.LOGGER.error("Failed to bind to address '{}', binding to wildcard IP instead", bindAddress);
                socket = new DatagramSocket(port);
            }
            socket.setTrafficClass(0x04); // IPTOS_RELIABILITY
        } catch (BindException e) {
            Voicechat.LOGGER.fatal("Failed to run voice chat at UDP port {}, make sure no other application is running at that port", port);
            Voicechat.LOGGER.fatal("Disabling Simple Voice Chat");
            Bukkit.getPluginManager().disablePlugin(Voicechat.INSTANCE);
            throw e;
        }
    }

    private void checkCorrectHost() throws Exception {
        String host = Voicechat.SERVER_CONFIG.voiceHost.get();
        if (!host.isEmpty()) {
            try {
                new URI("voicechat://" + host);
            } catch (URISyntaxException e) {
                Voicechat.LOGGER.warn("Failed to parse voice host: {}", e.getMessage());
                Voicechat.LOGGER.error("Shutting down server");
                Bukkit.shutdown();
                throw e;
            }
        }
    }

    @Override
    public RawUdpPacket read() throws Exception {
        if (socket == null) {
            throw new IllegalStateException("Socket not opened yet");
        }
        return read(socket);
    }

    @Override
    public void send(byte[] data, SocketAddress address) throws Exception {
        if (socket == null) {
            return; // Ignoring packet sending when socket isn't open yet
        }
        socket.send(new DatagramPacket(data, data.length, address));
    }

    @Override
    public int getLocalPort() {
        if (socket == null) {
            return -1;
        }
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
        if (socket == null) {
            return true;
        }
        return socket.isClosed();
    }
}
