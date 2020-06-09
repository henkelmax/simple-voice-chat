package de.maxhenkel.voicechat.voice.common;

import java.io.Serializable;
import java.util.UUID;

public class NetworkMessage<T extends Serializable> implements Serializable {

    private long timestamp;
    private long ttl;
    private T data;
    private UUID playerUUID;

    public NetworkMessage(T data) {
        this.data = data;
        this.timestamp = -1;
        this.ttl = 2000;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
