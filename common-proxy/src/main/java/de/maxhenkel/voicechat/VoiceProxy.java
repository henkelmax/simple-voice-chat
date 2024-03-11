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

    protected final VoiceProxySniffer voiceProxySniffer = new VoiceProxySniffer();

    private final VoiceChatLogger voiceChatLogger;

    protected VoiceProxyServer voiceProxyServer;

    private ProxyConfig voiceProxyConfig;

    public VoiceProxy(VoiceChatLogger logger) {
        this.voiceChatLogger = logger;
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
        if (!this.voiceProxySniffer.isPlayerReady(playerUUID)) return null;

        InetSocketAddress backendSocket = this.getDefaultBackendSocket(playerUUID);
        if (backendSocket == null) return null;

        Integer port = this.voiceProxySniffer.getServerPort(playerUUID);
        if (port == null) port = backendSocket.getPort();
        return new InetSocketAddress(backendSocket.getAddress(), port);
    }

    /**
     * Closes any existing VoiceProxyServer instance and starts a fresh VoiceProxyServer
     */
    protected void reloadVoiceProxyServer() {
        try {
            Files.createDirectories(this.getDataDirectory());
            Path configPath = this.getDataDirectory().resolve("voicechat-proxy.properties");
            this.voiceProxyConfig = ConfigBuilder.builder(ProxyConfig::new).path(configPath).build();
        } catch (Exception e) {
            this.voiceChatLogger.error("Error loading config", e);
        }

        if (this.voiceProxyServer != null) this.voiceProxyServer.interrupt();
        this.voiceProxyServer = new VoiceProxyServer(this);
        this.voiceProxyServer.start();
    }

    /**
     * Called whenever a player disconnects from a backend server
     *
     * @param playerUUID The UUID of the player that disconnected from a backend server
     */
    protected void onPlayerServerDisconnected(UUID playerUUID) {
        if (this.voiceProxyServer != null) this.voiceProxyServer.getVoiceProxyBridgeManager().disconnect(playerUUID);
        this.voiceProxySniffer.onPlayerServerDisconnect(playerUUID);
        this.getLogger().debug("Player {} is has disconnected from backend server, interrupting bridge if it exists", playerUUID);
    }

    public ProxyConfig getConfig() { return this.voiceProxyConfig; }

    public VoiceChatLogger getLogger() {
        return this.voiceChatLogger;
    }

    public VoiceProxySniffer getSniffer() {
        return this.voiceProxySniffer;
    }

}
