package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.net.SecretPacket;

import java.util.UUID;

public class InitializationData {

    private final String serverIP;
    private final int serverPort;
    private final UUID playerUUID;
    private final UUID secret;
    private final ServerConfig.Codec codec;
    private final int mtuSize;
    private final double voiceChatDistance;
    private final double voiceChatFadeDistance;
    private final double crouchDistanceMultiplier;
    private final int keepAlive;
    private final boolean groupsEnabled;
    private final boolean allowRecording;

    public InitializationData(String serverIP, UUID playerUUID, SecretPacket secretPacket) {
        this.serverIP = serverIP;
        this.serverPort = secretPacket.getServerPort();
        this.playerUUID = playerUUID;
        this.secret = secretPacket.getSecret();
        this.codec = secretPacket.getCodec();
        this.mtuSize = secretPacket.getMtuSize();
        this.voiceChatDistance = secretPacket.getVoiceChatDistance();
        this.voiceChatFadeDistance = secretPacket.getVoiceChatFadeDistance();
        this.crouchDistanceMultiplier = secretPacket.getCrouchDistanceMultiplier();
        this.keepAlive = secretPacket.getKeepAlive();
        this.groupsEnabled = secretPacket.groupsEnabled();
        this.allowRecording = secretPacket.allowRecording();
    }

    public String getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getSecret() {
        return secret;
    }

    public ServerConfig.Codec getCodec() {
        return codec;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public double getVoiceChatDistance() {
        return voiceChatDistance;
    }

    public double getVoiceChatFadeDistance() {
        return voiceChatFadeDistance;
    }

    public double getCrouchDistanceMultiplier() {
        return crouchDistanceMultiplier;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean groupsEnabled() {
        return groupsEnabled;
    }

    public boolean allowRecording() {
        return allowRecording;
    }
}
