package de.maxhenkel.voicechat.proxy.network;

import de.maxhenkel.voicechat.proxy.VoiceProxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VoiceProxyBridgeManager manages the bridge lifecycle for a given VoiceProxyServer.
 */
public class VoiceProxyBridgeManager {
    /**
     * A map of all currently connected players and their respective VoiceProxyBridge
     * Not all players connected to the Velocity proxy necessarily have a VoiceProxyBridge.
     */
    private final Map<UUID, VoiceProxyBridge> bridgeMap = new ConcurrentHashMap<>();

    /**
     * The instance that created this VoiceProxyBridgeManager
     */
    private final VoiceProxy voiceProxy;

    /**
     * The instance that created this VoiceProxyBridgeManager
     */
    private final VoiceProxyServer voiceProxyServer;

    /**
     * Determines whether this bridge is allowed to create new bridges
     */
    private boolean allowBridgeCreation = true;

    public VoiceProxyBridgeManager(VoiceProxy voiceProxy, VoiceProxyServer voiceProxyServer) {
        this.voiceProxy = voiceProxy;
        this.voiceProxyServer = voiceProxyServer;
    }

    /**
     * Disconnect the bridge for a given player
     * @param playerUUID Which player to disconnect the bridge for
     */
    public void disconnect(UUID playerUUID) {
        VoiceProxyBridge bridge = bridgeMap.getOrDefault(playerUUID, null);
        if (bridge != null) bridge.interrupt();
        bridgeMap.remove(playerUUID);
    }

    /**
     * Gets or creates a new bridge for a given player UUID
     * @param playerUUID Which player to get or create the bridge for
     * @param playerAddress Which address to relay the packets back to
     * @return The existing or newly created VoiceProxyBridge
     */
    public VoiceProxyBridge getOrCreateBridge(UUID playerUUID, SocketAddress playerAddress) {
        return bridgeMap.computeIfAbsent(playerUUID, uuid -> {
            if (!this.allowBridgeCreation) return null;

            SocketAddress serverAddress = this.voiceProxy.getBackendUDPSocket(playerUUID);
            if (serverAddress == null) return null;

            VoiceProxyBridge newBridge = new VoiceProxyBridge(uuid, playerAddress, serverAddress);
            newBridge.start();
            return newBridge;
        });
    }

    /**
     * Notifies all bridges to shut down and disallows the creation of further bridges
     */
    public void shutdown() {
        this.allowBridgeCreation = false;
        this.bridgeMap.values().forEach(VoiceProxyBridge::interrupt);
    }

    /**
     * The VoiceProxyBridge implements a single proxy connection from a velocity-connected player
     * to one of the velocity registered backend servers. The bridge lives for the duration of the
     * connection between the velocity player and the specific backend server.
     */
    public class VoiceProxyBridge extends Thread {
        /**
         * The connection between the Velocity proxy, acting as a player, to the backend server's UDP server
         */
        private DatagramSocket backendServerSocket;

        /**
         * The SocketAddress used by the player to connect to the Velocity UDP proxy.
         */
        private final SocketAddress playerAddress;

        /**
         * The UUID used by the player on the backend server.
         */
        private final UUID playerUUID;

        /**
         * The SocketAddress used by the velocity proxy to write to the backend server's UDP server.
         */
        private final SocketAddress serverAddress;

        public VoiceProxyBridge(UUID playerUUID, SocketAddress playerAddress, SocketAddress serverAddress) {
            this.playerUUID = playerUUID;
            this.playerAddress = playerAddress;
            this.serverAddress = serverAddress;
        }

        @Override
        public void interrupt() {
            bridgeMap.remove(this.playerUUID);
            this.backendServerSocket.close();
            super.interrupt();
        }

        /**
         * The Bridge will stay in a forwarding loop unless it is interrupted and/or the socket to the backend
         * server has been closed. A closure of the public-facing socket is handled by the VoiceProxyServer implementation.
         */
        @Override
        public void run() {
            try {
                this.backendServerSocket = new DatagramSocket();
                voiceProxy.getLogger().debug("Opened new DatagramSocket for communication with backend server");

                while(!this.isInterrupted() && !this.backendServerSocket.isClosed()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
                        this.backendServerSocket.receive(packet);

                        DatagramPacket proxyPacket = new DatagramPacket(packet.getData(), packet.getLength(), this.playerAddress);
                        voiceProxyServer.write(proxyPacket);
                    } catch (Exception e) {
                        if (!this.backendServerSocket.isClosed()) {
                            voiceProxy.getLogger().error("Failed to bridge packet from backend server to player", e);
                        } else {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                voiceProxy.getLogger().error("Failed to create DatagramSocket for backend communication, shutting down", e);
            }
            bridgeMap.remove(this.playerUUID);
        }

        /**
         * Forwards any given DatagramPacket from the player to the backend server
         * @param packet The DatagramPacket to be re-packaged and sent to the backend server
         */
        public void forward(DatagramPacket packet) throws IOException {
            if (this.backendServerSocket == null) return;
            if (this.backendServerSocket.isClosed()) return;
            this.backendServerSocket.send(new DatagramPacket(packet.getData(), packet.getLength(), this.serverAddress));
        }
    }

}
