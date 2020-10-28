package de.maxhenkel.voicechat.voice.server;

import java.net.InetAddress;
import java.util.UUID;

public class ClientConnection {

    private UUID playerUUID;
    private InetAddress address;
    private int port;

    public ClientConnection(UUID playerUUID, InetAddress address, int port) {
        this.playerUUID = playerUUID;
        this.address = address;
        this.port = port;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

}
