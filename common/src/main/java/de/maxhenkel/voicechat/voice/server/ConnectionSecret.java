package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.voice.common.Secret;

import java.util.UUID;

public class ConnectionSecret {

    private final UUID playerUUID;
    private final UUID connectionId;
    private final Secret secret;
    private boolean authenticated;

    public ConnectionSecret(UUID playerUUID, UUID connectionId, Secret secret) {
        this.playerUUID = playerUUID;
        this.connectionId = connectionId;
        this.secret = secret;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getConnectionId() {
        return connectionId;
    }

    public Secret getSecret() {
        return secret;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

}
