package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.voice.common.NetworkMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.UUID;

public class ClientConnection {

    private UUID playerUUID;
    private SocketAddress address;
    private long lastKeepAlive;
    private long lastKeepAliveResponse;

    public ClientConnection(UUID playerUUID, SocketAddress address) {
        this.playerUUID = playerUUID;
        this.address = address;
        this.lastKeepAlive = 0L;
        this.lastKeepAliveResponse = System.currentTimeMillis();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    public void setLastKeepAlive(long lastKeepAlive) {
        this.lastKeepAlive = lastKeepAlive;
    }

    public long getLastKeepAliveResponse() {
        return lastKeepAliveResponse;
    }

    public void setLastKeepAliveResponse(long lastKeepAliveResponse) {
        this.lastKeepAliveResponse = lastKeepAliveResponse;
    }

    public void send(DatagramSocket socket, NetworkMessage message) throws IOException {
        byte[] data = message.write();
        socket.send(new DatagramPacket(data, data.length, address));
    }
}
