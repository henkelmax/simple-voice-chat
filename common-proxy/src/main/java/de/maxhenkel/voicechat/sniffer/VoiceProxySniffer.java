package de.maxhenkel.voicechat.sniffer;

import de.maxhenkel.voicechat.VoiceProxy;

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
     * Maps a given player UUID to the sniffed UDP port.
     */
    private final Map<UUID, Integer> serverUDPPortMap = new ConcurrentHashMap<>();

    private final VoiceProxy voiceProxy;

    public VoiceProxySniffer(VoiceProxy voiceProxy) {
        this.voiceProxy = voiceProxy;
    }

    /**
     * Returns the players UUID on the proxy server.
     *
     * @param playerUUID the UUID of the player on the backend server
     * @return the UUID of the player on the proxy
     */
    public UUID getMappedPlayerUUID(UUID playerUUID) {
        return playerUUIDMap.getOrDefault(playerUUID, playerUUID);
    }

    /**
     * Returns the sniffed server UDP port or <code>null</code> if not found.
     *
     * @param playerUUID the UUID of the player on the proxy
     * @return the sniffed UDP port or <code>null</code>
     */
    public Integer getServerPort(UUID playerUUID) {
        return serverUDPPortMap.getOrDefault(playerUUID, null);
    }

    /**
     * Checks whether a player has completed the Secret handshake, and we are ready to proxy the connection.
     *
     * @param playerUUID the UUID of the player on the proxy
     * @return <code>true</code>> if the secret handshake was captured
     */
    public boolean isPlayerReady(UUID playerUUID) {
        return playerUUIDMap.containsValue(playerUUID);
    }

    /**
     * Called whenever a PluginMessage has been received by the proxy.
     *
     * @param channel    the channel on which the message was received
     * @param message    the contents of the received message
     * @param playerUUID the UUID of the player that sent or received the message
     * @return ByteBuffer if the plugin message should be replaced, <code>null</code> otherwise
     */
    public ByteBuffer onPluginMessage(String channel, ByteBuffer message, UUID playerUUID) {
        if (channel.endsWith(":secret")) {
            return handleSecretPacket(message, playerUUID);
        }
        return null;
    }

    /**
     * Called whenever a Player disconnects from a backend server.
     *
     * @param playerUUID the UUID of the player that disconnected
     */
    public void onPlayerServerDisconnect(UUID playerUUID) {
        serverUDPPortMap.remove(playerUUID);
        playerUUIDMap.remove(playerUUID);
    }

    /**
     * Called whenever a SecretPacket has been sniffed.
     *
     * @param message    the SecretPacket in bytes
     * @param playerUUID the UUID of the player this packet was intended for
     */
    private ByteBuffer handleSecretPacket(ByteBuffer message, UUID playerUUID) {
        SniffedSecretPacket packet = SniffedSecretPacket.fromBytes(message);
        playerUUIDMap.put(packet.getPlayerUUID(), playerUUID);
        serverUDPPortMap.put(playerUUID, packet.getServerPort());
        return packet.patch(voiceProxy);
    }

}
