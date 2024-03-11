package de.maxhenkel.voicechat.sniffer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VoiceProxySniffer implements a platform-independent way of sniffing
 * the Simple Voice Chat packets as they are transmitted through plugin channels.
 */
public class VoiceProxySniffer {

    /**
     * Maps the backend server player UUID to the proxy player UUID.
     * This is useful when UUID forwarding has not been properly configured.
     */
    private final Map<UUID, UUID> playerUUIDMap = new ConcurrentHashMap<>();

    /**
     * Maps a given player UUID to the sniffed UDP port
     */
    private final Map<UUID, Integer> serverUDPPortMap = new ConcurrentHashMap<>();

    /**
     * Returns the player's UUID on the proxy server
     *
     * @param playerUUID The UUID of the player on the backend server
     * @return The UUID of the player on the proxy
     */
    public UUID getMappedPlayerUUID(UUID playerUUID) {
        return this.playerUUIDMap.getOrDefault(playerUUID, playerUUID);
    }

    /**
     * Returns the sniffed server UDP port or null if not found
     *
     * @param playerUUID The UUID of the player on the proxy
     * @return The sniffed UDP port or null
     */
    public Integer getServerPort(UUID playerUUID) {
        return this.serverUDPPortMap.getOrDefault(playerUUID, null);
    }

    /**
     * Checks whether a player has completed the Secret handshake, and we are ready to proxy the connection
     *
     * @param playerUUID The UUID of the player on the proxy
     * @return True if the Secret handshake was captured
     */
    public boolean isPlayerReady(UUID playerUUID) {
        return this.playerUUIDMap.containsValue(playerUUID);
    }

    /**
     * Called whenever a PluginMessage has been received by the proxy
     *
     * @param channel    On which channel was the message received
     * @param message    The contents of the received message
     * @param playerUUID Which player was this message for or from
     */
    public void onPluginMessage(String channel, ByteBuffer message, UUID playerUUID) {
        if (channel.endsWith(":secret")) this.handleSecretPacket(message, playerUUID);
    }

    /**
     * Called whenever a Player disconnects from a backend server
     *
     * @param playerUUID Which player disconnected from a server
     */
    public void onPlayerServerDisconnect(UUID playerUUID) {
        this.serverUDPPortMap.remove(playerUUID);
        this.playerUUIDMap.remove(playerUUID);
    }

    /**
     * Called whenever a SecretPacket has been sniffed
     *
     * @param message    The SecretPacket in bytes
     * @param playerUUID The UUID of the player this packet was intended for
     */
    private void handleSecretPacket(ByteBuffer message, UUID playerUUID) {
        SniffedSecretPacket packet = SniffedSecretPacket.fromBytes(message);
        this.playerUUIDMap.put(packet.getPlayerUUID(), playerUUID);
        this.serverUDPPortMap.put(playerUUID, packet.getServerPort());
    }

}
