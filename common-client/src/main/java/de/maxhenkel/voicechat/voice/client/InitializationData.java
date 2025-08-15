package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.voice.common.Secret;

import java.net.URI;
import java.util.UUID;

public class InitializationData {

    private final String serverIP;
    private final int serverPort;
    private final UUID playerUUID;
    private final Secret secret;
    private final ServerConfig.Codec codec;
    private final int mtuSize;
    private final double voiceChatDistance;
    private final int keepAlive;
    private final boolean groupsEnabled;
    private final boolean allowRecording;

    public InitializationData(String serverIP, SecretPacket secretPacket) {
        HostData hostData = parseAddress(secretPacket.getVoiceHost(), serverIP, secretPacket.getServerPort());
        this.serverIP = hostData.ip;
        this.serverPort = hostData.port;
        this.playerUUID = secretPacket.getPlayerUUID();
        this.secret = secretPacket.getSecret();
        this.codec = secretPacket.getCodec();
        this.mtuSize = secretPacket.getMtuSize();
        this.voiceChatDistance = secretPacket.getVoiceChatDistance();
        this.keepAlive = secretPacket.getKeepAlive();
        this.groupsEnabled = secretPacket.groupsEnabled();
        this.allowRecording = secretPacket.allowRecording();
    }

    private static HostData parseAddress(String voiceHost, String serverIP, int serverPort) {
        String ip = serverIP;
        int port = serverPort;
        if (voiceHost.isEmpty()) {
            return new HostData(ip, port);
        }
        try {
            int parsedPort = Integer.parseInt(voiceHost);
            if (parsedPort <= 0 || parsedPort > 65535) {
                Voicechat.LOGGER.warn("Invalid voice host port: {}", parsedPort);
            } else {
                port = parsedPort;
            }
        } catch (NumberFormatException ignored) {
            try {
                URI uri = new URI("voicechat://" + voiceHost);
                String host = uri.getHost();
                int hostPort = uri.getPort();

                if (host != null) {
                    ip = host;
                }

                if (hostPort > 0) {
                    port = hostPort;
                }

            } catch (Exception e) {
                Voicechat.LOGGER.warn("Failed to parse voice host", e);
            }
        }
        return new HostData(ip, port);
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

    public Secret getSecret() {
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

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean groupsEnabled() {
        return groupsEnabled;
    }

    public boolean allowRecording() {
        return allowRecording;
    }

    private static class HostData {
        private final String ip;
        private final int port;

        public HostData(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }

}
