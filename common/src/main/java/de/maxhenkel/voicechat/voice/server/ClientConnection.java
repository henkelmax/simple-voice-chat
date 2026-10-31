package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.voice.common.NetworkMessage;

import java.net.SocketAddress;
import java.util.UUID;

public class ClientConnection {

    private final ConnectionSecret connectionSecret;
    private final SocketAddress address;
    private long lastKeepAliveResponse;
    private volatile boolean initialized;

    public ClientConnection(ConnectionSecret connectionSecret, SocketAddress address) {
        this.connectionSecret = connectionSecret;
        this.address = address;
        this.lastKeepAliveResponse = System.currentTimeMillis();
    }

    public UUID getPlayerUUID() {
        return connectionSecret.getPlayerUUID();
    }

    public ConnectionSecret getConnectionSecret() {
        return connectionSecret;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public long getLastKeepAliveResponse() {
        return lastKeepAliveResponse;
    }

    public void setLastKeepAliveResponse(long lastKeepAliveResponse) {
        this.lastKeepAliveResponse = lastKeepAliveResponse;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public void send(Server server, NetworkMessage message) throws Exception {
        server.getSocket().send(message.writeServer(this), address);
    }

}
