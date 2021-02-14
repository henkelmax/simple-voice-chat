package de.maxhenkel.voicechat.voice.server;

import java.net.SocketAddress;
import java.util.UUID;

public class ClientConnection {

    private UUID playerUUID;
    private SocketAddress address;

    public ClientConnection(UUID playerUUID, SocketAddress address) {
        this.playerUUID = playerUUID;
        this.address = address;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public SocketAddress getAddress() {
        return address;
    }

}
