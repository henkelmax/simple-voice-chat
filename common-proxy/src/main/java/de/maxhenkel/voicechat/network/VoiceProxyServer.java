package de.maxhenkel.voicechat.network;

import de.maxhenkel.voicechat.VoiceProxy;
import de.maxhenkel.voicechat.debug.PingHandler;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * The VoiceProxyServer implements the publicly facing UDP server which then proxies
 * the UDP traffic to the appropriate backend server's Simple Voice Chat UDP server.
 */
public class VoiceProxyServer extends Thread {

    /**
     * The instance that created this VoiceProxyServer
     */
    private final VoiceProxy voiceProxy;

    /**
     * Manages all the VoiceProxyBridge instances for this particular VoiceProxyServer
     */
    private final VoiceProxyBridgeManager voiceProxyBridgeManager;

    /**
     * The public UDP socket of the VoiceProxyServer. This is where Minecraft SimpleVoiceChat clients will connect to.
     */
    private volatile DatagramSocket socket;

    public VoiceProxyServer(VoiceProxy proxy) {
        setDaemon(true);
        setName("VoiceProxyServer");

        voiceProxy = proxy;
        voiceProxyBridgeManager = new VoiceProxyBridgeManager(voiceProxy, this);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (socket != null) {
            socket.close();
        }
    }

    @Override
    public void run() {
        try {
            socket = openSocket();

            while (!isInterrupted() && !socket.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
                    socket.receive(packet);
                    handlePacket(packet);
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        voiceProxy.getLogger().debug("An exception occurred while handling an incoming datagram", e);
                    }
                }
            }
        } catch (Throwable e) {
            voiceProxy.getLogger().error("The voice chat proxy server encountered a fatal error and has been shut down", e);
        } finally {
            // interrupt() might have run before the socket existed, so it has to be closed here
            if (socket != null) {
                socket.close();
            }
            // No packets are handled anymore, so no new bridges can be created from here on
            voiceProxyBridgeManager.shutdown();
        }
    }

    private DatagramSocket openSocket() throws SocketException {
        int port = voiceProxy.getPort();

        String bindAddress = voiceProxy.getConfig().bindAddress.get();
        InetAddress address = null;
        if (bindAddress.isEmpty()) {
            address = voiceProxy.getDefaultBindSocket().getAddress();
            bindAddress = address.getHostAddress();
        } else if (!bindAddress.trim().equals("*")) {
            try {
                address = InetAddress.getByName(bindAddress);
            } catch (Exception e) {
                voiceProxy.getLogger().error("An invalid bind address was specified in the config '{}', falling back to proxy bind address", bindAddress);
                address = voiceProxy.getDefaultBindSocket().getAddress();
                bindAddress = address.getHostAddress();
            }
        }

        try {
            DatagramSocket newSocket = new DatagramSocket(port, address);
            voiceProxy.getLogger().info("Voice chat proxy server started at {}:{}", bindAddress, port);
            return newSocket;
        } catch (BindException e) {
            if (address == null || bindAddress.equals("0.0.0.0")) {
                throw e;
            }
            voiceProxy.getLogger().error("Failed to bind to address '{}', binding to wildcard IP instead", bindAddress);
            return new DatagramSocket(port);
        }
    }

    /**
     * Handles a single incoming datagram by figuring out which player it belongs to and relaying it to the appropriate backend server.
     * Any invalid datagram packets will be discarded silently.
     *
     * @param packet The datagram that was received on the public UDP socket
     */
    private void handlePacket(DatagramPacket packet) throws IOException {
        // The first byte in the datagram must match the magic byte, else this is not a valid SimpleVoiceChat packet
        ByteBuffer bb = ByteBuffer.wrap(packet.getData());
        if (bb.get() != (byte) 0b11111111) {
            return;
        }

        // The Player UUID comes right after the magic byte in the form of two longs
        UUID playerUuid = new UUID(bb.getLong(), bb.getLong());

        if (PingHandler.onPacket(this, packet.getSocketAddress(), playerUuid, bb)) {
            return;
        }

        playerUuid = voiceProxy.getSniffer().getMappedPlayerUUID(playerUuid);

        VoiceProxyBridgeManager.VoiceProxyBridge bridge = voiceProxyBridgeManager.getOrCreateBridge(playerUuid, packet.getSocketAddress());
        if (bridge == null) {
            return;
        }

        bridge.forward(packet);
    }

    public VoiceProxyBridgeManager getVoiceProxyBridgeManager() {
        return voiceProxyBridgeManager;
    }

    public VoiceProxy getVoiceProxy() {
        return voiceProxy;
    }

    /**
     * Writes a DatagramPacket out via the public UDP socket. It is assumed that the datagram is already addressed to the correct target, no modification will be performed.
     *
     * @param packet The DatagramPacket to write out via the public UDP socket
     */
    public void write(DatagramPacket packet) {
        if (socket == null || socket.isClosed()) {
            return;
        }
        try {
            socket.send(packet);
        } catch (Exception e) {
            voiceProxy.getLogger().debug("An exception occurred while writing an outgoing datagram", e);
        }
    }
}
