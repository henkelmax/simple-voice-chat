package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.voice.common.Secret;
import io.netty.channel.local.LocalAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InitializationData {

    private final String serverIP;
    private final int serverPort;
    private final UUID playerUUID;
    private final Secret secret;
    private final UUID connectionId;
    private final ServerConfig.Codec codec;
    private final int mtuSize;
    private final double voiceChatDistance;
    private final int keepAlive;
    private final boolean groupsEnabled;
    private final boolean allowRecording;

    private InitializationData(HostData hostData, SecretPacket secretPacket) {
        this.serverIP = hostData.ip;
        this.serverPort = hostData.port;
        this.playerUUID = secretPacket.getPlayerUUID();
        this.secret = secretPacket.getSecret();
        this.connectionId = secretPacket.getConnectionId();
        this.codec = secretPacket.getCodec();
        this.mtuSize = secretPacket.getMtuSize();
        this.voiceChatDistance = secretPacket.getVoiceChatDistance();
        this.keepAlive = secretPacket.getKeepAlive();
        this.groupsEnabled = secretPacket.groupsEnabled();
        this.allowRecording = secretPacket.allowRecording();
    }

    public static CompletableFuture<InitializationData> create(SocketAddress socketAddress, @Nullable ServerData serverData, SecretPacket secretPacket) throws IOException {
        HostData hostData = parseAddress(secretPacket.getVoiceHost(), resolveAddress(socketAddress), secretPacket.getServerPort());
        if (!secretPacket.getVoiceHost().isEmpty() || serverData == null) {
            return CompletableFuture.completedFuture(new InitializationData(hostData, secretPacket));
        }
        String host = ServerAddress.parseString(serverData.ip).getHost();
        return CompletableFuture.supplyAsync(() -> {
            ServerAddress srvAddress = SrvLookup.lookup(host);
            if (srvAddress == null) {
                return new InitializationData(hostData, secretPacket);
            }
            Voicechat.LOGGER.info("Using voicechat SRV record to connect to server '{}:{}'", srvAddress.getHost(), srvAddress.getPort());
            return new InitializationData(new HostData(srvAddress.getHost(), srvAddress.getPort()), secretPacket);
        }, Util.ioPool()).exceptionally(e -> {
            Voicechat.LOGGER.warn("Failed to look up voicechat SRV record", e);
            return new InitializationData(hostData, secretPacket);
        });
    }

    private static String resolveAddress(SocketAddress socketAddress) throws IOException {
        if (socketAddress instanceof LocalAddress) {
            return "127.0.0.1";
        }
        if (!(socketAddress instanceof InetSocketAddress address)) {
            throw new IOException(String.format("Failed to determine server address with SocketAddress of type %s", socketAddress.getClass().getSimpleName()));
        }
        InetAddress inetAddress = address.getAddress();
        if (inetAddress == null) {
            return address.getHostString();
        }
        return inetAddress.getHostAddress();
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

    public UUID getConnectionId() {
        return connectionId;
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
