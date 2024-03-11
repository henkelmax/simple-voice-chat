package de.maxhenkel.voicechat.proxy;

import de.maxhenkel.voicechat.proxy.config.VoiceProxyConfig;
import de.maxhenkel.voicechat.proxy.logging.VoiceChatLogger;
import de.maxhenkel.voicechat.proxy.network.VoiceProxyServer;
import de.maxhenkel.voicechat.proxy.sniffer.VoiceProxySniffer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public abstract class VoiceProxy {
    /**
     * Generic helper to sniff PluginMessage traffic on proxies
     */
    protected final VoiceProxySniffer voiceProxySniffer = new VoiceProxySniffer();

    /**
     * The currently active VoiceProxyServer instance
     */
    protected VoiceProxyServer voiceProxyServer;

    /**
     * Determine which SocketAddress to use for backend UDP traffic
     * @param playerUUID Which player to find the socket for
     * @return The sniffed SocketAddress or the game port used by the server
     */
    public SocketAddress getBackendUDPSocket(UUID playerUUID) {
        if (!this.voiceProxySniffer.isPlayerReady(playerUUID)) return null;

        InetSocketAddress backendSocket = this.getBackendSocket(playerUUID);
        if (backendSocket == null) return null;

        Integer port = this.voiceProxySniffer.getServerPort(playerUUID);
        if (port == null) port = backendSocket.getPort();
        return new InetSocketAddress(backendSocket.getAddress(), port);
    }

    /**
     * Closes any existing VoiceProxyServer instance and starts a fresh VoiceProxyServer
     */
    protected void reloadVoiceProxyServer() {
        if (this.voiceProxyServer != null) this.voiceProxyServer.interrupt();
        this.voiceProxyServer = new VoiceProxyServer(this);
        this.voiceProxyServer.start();
    }

    /**
     * Called whenever a player disconnects from a backend server
     * @param playerUUID The UUID of the player that disconnected from a backend server
     */
    protected void onPlayerServerDisconnected(UUID playerUUID) {
        if (this.voiceProxyServer != null) this.voiceProxyServer.getVoiceProxyBridgeManager().disconnect(playerUUID);
        this.voiceProxySniffer.onPlayerServerDisconnect(playerUUID);
        this.getLogger().debug("Player {} is has disconnected from backend server, interrupting bridge if it exists", playerUUID);
    }

    /**
     * Returns the currently active configuration for the VoiceProxy
     */
    public abstract VoiceProxyConfig getConfig();

    /**
     * Returns the root logger for the VoiceProxy
     */
    public abstract VoiceChatLogger getLogger();

    /**
     * Determine which SocketAddress is used by the player to communicate with the game server
     * @param playerUUID Which player to find the socket for
     * @return The SocketAddress used for game traffic between the game server and the proxy
     */
    protected abstract InetSocketAddress getBackendSocket(UUID playerUUID);


}
