package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.ProxyConfig;
import de.maxhenkel.voicechat.logging.VoiceChatLogger;
import de.maxhenkel.voicechat.network.VoiceProxyServer;
import de.maxhenkel.voicechat.sniffer.VoiceProxySniffer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public abstract class VoiceProxy {

    public static final String MOD_VERSION = BuildConstants.MOD_VERSION;

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
        return new InetSocketAddress(backendSocket.getAddress(), port);
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

        if (voiceProxyServer != null) {
            voiceProxyServer.interrupt();
        }
        voiceProxyServer = new VoiceProxyServer(this);
        voiceProxyServer.start();
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
