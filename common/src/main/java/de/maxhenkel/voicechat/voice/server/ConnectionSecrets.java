package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.voice.common.Secret;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the connection secrets of all players, looked up either by player UUID or by connection ID.
 * Writes are synchronized, so the two lookups can never drift apart.
 */
public class ConnectionSecrets {

    private final Map<UUID, ConnectionSecret> byPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, ConnectionSecret> byConnectionId = new ConcurrentHashMap<>();

    /**
     * Creates a new secret and connection ID for the player.
     *
     * @param playerUUID the player UUID
     * @return the new connection secret or null if the player already has one
     */
    @Nullable
    public synchronized ConnectionSecret create(UUID playerUUID) {
        if (byPlayer.containsKey(playerUUID)) {
            return null;
        }
        ConnectionSecret connectionSecret = new ConnectionSecret(playerUUID, Utils.generateSecureRandomUUID(), Secret.generateNewRandomSecret());
        byConnectionId.put(connectionSecret.getConnectionId(), connectionSecret);
        byPlayer.put(playerUUID, connectionSecret);
        return connectionSecret;
    }

    @Nullable
    public ConnectionSecret get(UUID playerUUID) {
        return byPlayer.get(playerUUID);
    }

    @Nullable
    public ConnectionSecret getByConnectionId(UUID connectionId) {
        return byConnectionId.get(connectionId);
    }

    public synchronized void remove(UUID playerUUID) {
        ConnectionSecret connectionSecret = byPlayer.remove(playerUUID);
        if (connectionSecret != null) {
            byConnectionId.remove(connectionSecret.getConnectionId());
        }
    }

    public synchronized void clear() {
        byPlayer.clear();
        byConnectionId.clear();
    }

}
