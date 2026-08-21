package de.maxhenkel.voicechat.network;

import de.maxhenkel.voicechat.VoiceProxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VoiceProxyBridgeManager manages the bridge lifecycle for a given VoiceProxyServer.
 */
public class VoiceProxyBridgeManager {

    /**
     * How many consecutive errors a bridge tolerates before it gives up
     */
    private static final int MAX_CONSECUTIVE_ERRORS = 100;

    /**
     * How long a bridge waits before retrying after an error
     */
    private static final int ERROR_RETRY_DELAY = 50;

    /**
     * A map of all currently connected players and their respective VoiceProxyBridge
     * Not all players connected to the Velocity proxy necessarily have a VoiceProxyBridge.
     */
    private final Map<UUID, VoiceProxyBridge> bridgeMap = new ConcurrentHashMap<>();

    private final VoiceProxy voiceProxy;

    private final VoiceProxyServer voiceProxyServer;

    public VoiceProxyBridgeManager(VoiceProxy voiceProxy, VoiceProxyServer voiceProxyServer) {
        this.voiceProxy = voiceProxy;
        this.voiceProxyServer = voiceProxyServer;
    }

    /**
     * Disconnect the bridge for a given player
     *
     * @param playerUUID Which player to disconnect the bridge for
     */
    public void disconnect(UUID playerUUID) {
        VoiceProxyBridge bridge = bridgeMap.get(playerUUID);
        if (bridge != null) {
            bridge.interrupt();
        }
    }

    /**
     * Gets or creates a new bridge for a given player UUID
     *
     * @param playerUUID    Which player to get or create the bridge for
     * @param playerAddress Which address to relay the packets back to
     * @return The existing or newly created VoiceProxyBridge, <code>null</code> if none could be created
     */
    public VoiceProxyBridge getOrCreateBridge(UUID playerUUID, SocketAddress playerAddress) {
        return bridgeMap.computeIfAbsent(playerUUID, uuid -> {
            InetSocketAddress serverAddress = voiceProxy.getSniffer().getBackendSocket(uuid);
            if (serverAddress == null) {
                return null;
            }

            try {
                VoiceProxyBridge newBridge = new VoiceProxyBridge(uuid, playerAddress, serverAddress);
                newBridge.start();
                return newBridge;
            } catch (SocketException e) {
                voiceProxy.getLogger().error("Failed to create DatagramSocket", e);
                // No socket can be created, so the player has to negotiate a new secret to get a working bridge
                voiceProxy.getSniffer().resetBackendSocket(uuid);
                return null;
            }
        });
    }

    /**
     * Notifies all bridges to shut down.
     * This must only be called once no more packets are handled, as it does not prevent the creation of new bridges.
     */
    public void shutdown() {
        bridgeMap.values().forEach(VoiceProxyBridge::interrupt);
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
        private final DatagramSocket backendServerSocket;

        /**
         * The SocketAddress used by the player to connect to the Velocity UDP proxy.
         */
        private final SocketAddress playerAddress;

        /**
         * The UUID used by the player on the proxy server.
         */
        private final UUID playerUUID;

        /**
         * The SocketAddress used by the velocity proxy to write to the backend server's UDP server.
         * This is unresolved until the bridge thread has resolved it.
         */
        private volatile InetSocketAddress serverAddress;

        public VoiceProxyBridge(UUID playerUUID, SocketAddress playerAddress, InetSocketAddress serverAddress) throws SocketException {
            setDaemon(true);
            setName(String.format("VoiceProxyBridge-%s", playerUUID));

            this.playerUUID = playerUUID;
            this.playerAddress = playerAddress;
            this.serverAddress = serverAddress;
            this.backendServerSocket = new DatagramSocket();
        }

        @Override
        public void interrupt() {
            bridgeMap.remove(playerUUID, this);
            backendServerSocket.close();
            super.interrupt();
        }

        /**
         * The Bridge will stay in a forwarding loop unless it is interrupted and/or the socket to the backend
         * server has been closed. A closure of the public-facing socket is handled by the VoiceProxyServer implementation.
         */
        @Override
        public void run() {
            if (!resolveServerAddress()) {
                voiceProxy.getLogger().warn("Failed to resolve backend server address '{}' of player {}", serverAddress.getHostString(), playerUUID);
                // The player has to negotiate a new secret to get a working bridge
                voiceProxy.getSniffer().resetBackendSocket(playerUUID);
                interrupt();
                return;
            }

            int consecutiveErrors = 0;
            while (!isInterrupted() && !backendServerSocket.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
                    backendServerSocket.receive(packet);
                    consecutiveErrors = 0;

                    voiceProxyServer.write(new DatagramPacket(packet.getData(), packet.getLength(), playerAddress));
                } catch (Exception e) {
                    if (backendServerSocket.isClosed()) {
                        break;
                    }
                    consecutiveErrors++;
                    if (consecutiveErrors == 1) {
                        voiceProxy.getLogger().warn("Failed to bridge packet from backend server to player {}, retrying", playerUUID, e);
                    }
                    if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                        voiceProxy.getLogger().error("Closing the bridge of player {} after {} consecutive errors", playerUUID, consecutiveErrors, e);
                        // The player has to negotiate a new secret to get a working bridge
                        voiceProxy.getSniffer().resetBackendSocket(playerUUID);
                        interrupt();
                    } else {
                        try {
                            Thread.sleep(ERROR_RETRY_DELAY);
                        } catch (InterruptedException interrupted) {
                            break;
                        }
                    }
                }
            }
            bridgeMap.remove(playerUUID, this);
        }

        /**
         * Resolves the address of the backend server if it is not resolved yet.
         *
         * @return <code>true</code> if the address can be used to send packets to
         */
        private boolean resolveServerAddress() {
            if (!serverAddress.isUnresolved()) {
                return true;
            }
            InetSocketAddress resolved = new InetSocketAddress(serverAddress.getHostString(), serverAddress.getPort());
            if (resolved.isUnresolved()) {
                return false;
            }
            serverAddress = resolved;
            return true;
        }

        /**
         * Forwards any given DatagramPacket from the player to the backend server
         *
         * @param packet The DatagramPacket to be re-packaged and sent to the backend server
         */
        public void forward(DatagramPacket packet) throws IOException {
            InetSocketAddress address = serverAddress;
            if (backendServerSocket.isClosed() || address.isUnresolved()) {
                return;
            }
            backendServerSocket.send(new DatagramPacket(packet.getData(), packet.getLength(), address));
        }
    }

}
