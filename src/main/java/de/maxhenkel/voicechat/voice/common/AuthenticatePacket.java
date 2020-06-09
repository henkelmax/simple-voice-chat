package de.maxhenkel.voicechat.voice.common;

import java.io.Serializable;
import java.util.UUID;

public class AuthenticatePacket implements Serializable {

    private UUID playerUUID;
    private UUID secret;

    public AuthenticatePacket(UUID playerUUID, UUID secret) {
        this.playerUUID = playerUUID;
        this.secret = secret;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getSecret() {
        return secret;
    }
}
