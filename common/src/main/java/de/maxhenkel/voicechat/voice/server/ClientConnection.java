package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.voice.common.NetworkMessage;

import java.net.SocketAddress;
import java.util.UUID;

public class ClientConnection {

    protected final ConnectionSecret connectionSecret;
    protected final SocketAddress address;
    protected long lastKeepAliveResponse;

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

    public void send(Server server, NetworkMessage message) throws Exception {
        server.getSocket().send(message.writeServer(this), address);
    }

}
