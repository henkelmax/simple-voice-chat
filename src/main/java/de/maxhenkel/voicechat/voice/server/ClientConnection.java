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
    private long sequenceNumber;
    private long lastClientSequenceNumber;
    private long lastKeepAlive;
    private long lastKeepAliveResponse;

    public ClientConnection(UUID playerUUID, SocketAddress address) {
        this.playerUUID = playerUUID;
        this.address = address;
        this.sequenceNumber = 0L;
        this.lastClientSequenceNumber = -1L;
        this.lastKeepAlive = 0L;
        this.lastKeepAliveResponse = System.currentTimeMillis();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public long getAndIncreaseSequenceNumber() {
        long num = sequenceNumber;
        sequenceNumber++;
        return num;
    }

    public long getLastClientSequenceNumber() {
        return lastClientSequenceNumber;
    }

    public void setLastClientSequenceNumber(long lastClientSequenceNumber) {
        this.lastClientSequenceNumber = lastClientSequenceNumber;
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
        byte[] data = message.write(getAndIncreaseSequenceNumber());
        socket.send(new DatagramPacket(data, data.length, address));
    }

}
