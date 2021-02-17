package de.maxhenkel.voicechat.voice.server;

import java.net.SocketAddress;
import java.util.UUID;

public class ClientConnection {

    private UUID playerUUID;
    private SocketAddress address;
    private long sequenceNumber;
    private long lastClientSequenceNumber;

    public ClientConnection(UUID playerUUID, SocketAddress address) {
        this.playerUUID = playerUUID;
        this.address = address;
        this.sequenceNumber = 0L;
        this.lastClientSequenceNumber = -1L;
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
}
