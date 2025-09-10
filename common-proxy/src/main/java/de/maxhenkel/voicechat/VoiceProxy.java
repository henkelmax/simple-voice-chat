package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.command.CommandSender;
import de.maxhenkel.voicechat.config.ProxyConfig;
import de.maxhenkel.voicechat.logging.VoiceChatLogger;
import de.maxhenkel.voicechat.network.VoiceProxyServer;
import de.maxhenkel.voicechat.sniffer.VoiceProxySniffer;
import de.maxhenkel.voicechat.util.BackendServer;
import de.maxhenkel.voicechat.util.PingManager;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public abstract class VoiceProxy {

    public static final String MOD_ID = "voicechat";
    public static final String VOICECHAT_COMMAND = "voicechatproxy";
    public static final String PING_PERMISSION = "voicechat.ping";
    public static final String MOD_VERSION = BuildConstants.MOD_VERSION;
    public static final int COMPATIBILITY_VERSION = BuildConstants.COMPATIBILITY_VERSION;

    public static final String SECRET_CHANNEL = "voicechat:secret";
    public static final String SECRET_CHANNEL_1_12 = "vc:secret";
    public static final String REQUEST_SECRET_CHANNEL = "voicechat:request_secret";
    public static final String REQUEST_SECRET_CHANNEL_1_12 = "vc:request_secret";

    protected final VoiceProxySniffer voiceProxySniffer = new VoiceProxySniffer(this);
    private final VoiceChatLogger voiceChatLogger;
    protected VoiceProxyServer voiceProxyServer;
    private ProxyConfig voiceProxyConfig;

    public VoiceProxy(VoiceChatLogger logger) {
        voiceChatLogger = logger;
    }

    /**
     * Determine which SocketAddress is used by the player to communicate with the game server
     *
     * @param playerUUID Which player to find the socket for
     * @return The SocketAddress used for game traffic between the game server and the proxy
     */
    public abstract InetSocketAddress getDefaultBackendSocket(UUID playerUUID);

    /**
     * Determine which SocketAddress is used by the proxy to bind its game port on.
     */
    public abstract InetSocketAddress getDefaultBindSocket();

    /**
     * Returns the Path to the data / config directory for the proxy server plugin
     */
    public abstract Path getDataDirectory();

    public abstract List<BackendServer> getBackendServers();

    /**
     * Determine which SocketAddress to use for backend UDP traffic
     *
     * @param playerUUID Which player to find the socket for
     * @return The sniffed SocketAddress or the game port used by the server
     */
    public SocketAddress getBackendUDPSocket(UUID playerUUID) {
        if (!voiceProxySniffer.isPlayerReady(playerUUID)) {
            return null;
        }

        InetSocketAddress backendSocket = getDefaultBackendSocket(playerUUID);
        if (backendSocket == null) {
            return null;
        }

        Integer port = voiceProxySniffer.getServerPort(playerUUID);
        if (port == null) {
            port = backendSocket.getPort();
        }
        return new InetSocketAddress(backendSocket.getHostString(), port);
    }

    /**
     * Returns which port to use for the VoiceProxyServer
     */
    public int getPort() {
        int port = getConfig().port.get();
        if (port == -1) {
            port = getDefaultBindSocket().getPort();
        }
        return port;
    }

    /**
     * Closes any existing VoiceProxyServer instance and starts a fresh VoiceProxyServer
     */
    protected void reloadVoiceProxyServer() {
        try {
            Files.createDirectories(getDataDirectory());
            Path configPath = getDataDirectory().resolve("voicechat-proxy.properties");
            voiceProxyConfig = ConfigBuilder.builder(ProxyConfig::new).path(configPath).build();
        } catch (Exception e) {
            voiceChatLogger.error("Error loading config", e);
        }
        checkCorrectHost();

        if (voiceProxyServer != null) {
            voiceProxyServer.interrupt();
        }
        voiceProxyServer = new VoiceProxyServer(this);
        voiceProxyServer.start();
    }

    private void checkCorrectHost() {
        String host = voiceProxyConfig.voiceHost.get();
        if (!host.isEmpty()) {
            try {
                new URI("voicechat://" + host);
                voiceChatLogger.info("Voice host is '{}'", host);
            } catch (URISyntaxException e) {
                voiceChatLogger.warn("Failed to parse voice host", e);
            }
        }
    }

    /**
     * Called whenever a player disconnects from a backend server
     *
     * @param playerUUID The UUID of the player that disconnected from a backend server
     */
    protected void onPlayerServerDisconnected(UUID playerUUID) {
        if (voiceProxyServer != null) {
            voiceProxyServer.getVoiceProxyBridgeManager().disconnect(playerUUID);
        }
        voiceProxySniffer.onPlayerServerDisconnect(playerUUID);
        getLogger().debug("Player {} is has disconnected from backend server, interrupting bridge if it exists", playerUUID);
    }

    protected void onVoicechatCommand(CommandSender sender, String[] args) {
        if (args.length <= 0) {
            sender.sendMessage("Usage: /voicechatproxy ping");
            return;
        }
        if (!args[0].equalsIgnoreCase("ping")) {
            sender.sendMessage("Unknown command");
            return;
        }
        if (!sender.hasPermission(PING_PERMISSION)) {
            sender.sendMessage("You do not have permission to use this command");
            return;
        }
        List<BackendServer> backendServers = getBackendServers();
        if (args.length <= 1) {
            if (backendServers.isEmpty()) {
                sender.sendMessage("No servers available to ping");
                return;
            }
            sender.sendMessage("Servers available to ping:");
            for (BackendServer backendServer : backendServers) {
                sender.sendMessage(String.format("  - %s (%s)", backendServer.getName(), backendServer.getAddress()));
            }
            sender.sendMessage("Usage: /voicechatproxy ping <server name> <voice chat port>");
            return;
        }
        if (args.length <= 2) {
            sender.sendMessage("No voice chat port provided");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("Too many arguments");
            return;
        }

        String serverName = args[1];
        int voiceChatPort;
        try {
            voiceChatPort = Integer.parseInt(args[2]);
            if (voiceChatPort <= 0 || voiceChatPort > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(String.format("Invalid voice chat port: %s", args[2]));
            return;
        }

        BackendServer server = backendServers
                .stream()
                .filter(s -> s.getName().equals(serverName))
                .findFirst()
                .orElseGet(() -> backendServers
                        .stream()
                        .filter(s -> s.getName().equalsIgnoreCase(serverName))
                        .findFirst()
                        .orElse(null)
                );

        if (server == null) {
            sender.sendMessage(String.format("Server '%s' not found", serverName));
            return;
        }

        try {
            PingManager.sendPing(PingManager.withPort(server.getAddress(), voiceChatPort), 10, new PingManager.PingListener() {
                @Override
                public void onSend(int attempts) {
                    sender.sendMessage(String.format("Sending ping to server '%s'", server.getName()));
                }

                @Override
                public void onSuccessfulAttempt(int attempts, long pingMilliseconds) {
                    sender.sendMessage(String.format("Received response from server '%s' in %s ms", server.getName(), pingMilliseconds));
                }

                @Override
                public void onFailedAttempt(int attempts) {
                    sender.sendMessage(String.format("No response from server '%s'", server.getName()));
                }

                @Override
                public void onFinish(int successfulAttempts, int timeoutAttempts, long pingMilliseconds) {
                    if (successfulAttempts > 0) {
                        if (timeoutAttempts > 0) {
                            sender.sendMessage(String.format("Connection check unstable for server '%s' (Ping: %s ms, %s/%s attempts timed out)", server.getName(), pingMilliseconds, timeoutAttempts, successfulAttempts + timeoutAttempts));
                        } else {
                            sender.sendMessage(String.format("Connection check successful for server '%s' (Ping: %s ms)", server.getName(), pingMilliseconds));
                        }
                    } else {
                        sender.sendMessage(String.format("Connection check failed for server '%s'", server.getName()));
                    }
                }

                @Override
                public void onError(Exception e) {
                    sender.sendMessage(String.format("Error pinging server '%s': %s", server.getName(), e.getMessage()));
                }

            });
        } catch (Exception e) {
            sender.sendMessage(String.format("Error pinging server '%s': %s", server.getName(), e.getMessage()));
        }
    }

    public ProxyConfig getConfig() {
        return voiceProxyConfig;
    }

    public VoiceChatLogger getLogger() {
        return voiceChatLogger;
    }

    public VoiceProxySniffer getSniffer() {
        return voiceProxySniffer;
    }

}
