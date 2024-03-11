package de.maxhenkel.voicechat.proxy.sniffer;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * A simplified version of the SecretPacket which the proxy can use to sniff the required configuration
 */
public class SniffedSecretPacket {

    private UUID playerUUID;
    private UUID secret;
    private int serverPort;

    public static SniffedSecretPacket fromBytes(ByteBuffer buffer) {
        SniffedSecretPacket packet = new SniffedSecretPacket();
        packet.secret = new UUID(buffer.getLong(), buffer.getLong());
        packet.serverPort = buffer.getInt();
        packet.playerUUID = new UUID(buffer.getLong(), buffer.getLong());
        return packet;
    }

    /**
     * Returns the player UUID on the backend server
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Returns the secret used for the connection between the player and the backend server
     */
    public UUID getSecret() {
        return this.secret;
    }

    /**
     * Returns the UDP port of the backend server
     */
    public int getServerPort() {
        return serverPort;
    }
}
