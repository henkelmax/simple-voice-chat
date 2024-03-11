package de.maxhenkel.voicechat.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.maxhenkel.voicechat.BuildConstants;
import de.maxhenkel.voicechat.proxy.VoiceProxy;
import de.maxhenkel.voicechat.proxy.config.VoiceProxyConfig;
import de.maxhenkel.voicechat.proxy.logging.VoiceChatLogger;
import de.maxhenkel.voicechat.velocity.config.VelocityVoiceProxyConfig;
import de.maxhenkel.voicechat.velocity.logging.JavaLoggingLogger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(
        id = "simplevoicechat-velocity",
        name = "SimpleVoiceChatVelocity",
        version = BuildConstants.MOD_VERSION,
        authors = "henkelmax, NilaTheDragon",
        url = "https://github.com/henkelmax/simple-voice-chat",
        description = "Run multiple servers with Simple Voice Chat behind a single public port"
)
public class SimpleVoiceChatVelocity extends VoiceProxy {
    /**
     * VoiceChatLogger wraps the Logger given by Velocity
     */
    private final VoiceChatLogger voiceChatLogger;

    @DataDirectory
    @Inject
    private Path dataDirectory;
    @Inject
    private ProxyServer proxyServer;
    private VoiceProxyConfig voiceProxyConfig;

    @Inject
    public SimpleVoiceChatVelocity(Logger logger) {
        this.voiceChatLogger = new JavaLoggingLogger(logger);
    }

    @Override
    protected InetSocketAddress getBackendSocket(UUID playerUUID) {
        Optional<Player> player = this.proxyServer.getPlayer(playerUUID);
        if (player.isEmpty()) return null;

        Optional<ServerConnection> server = player.get().getCurrentServer();
        return server.map(serverConnection -> serverConnection.getServerInfo().getAddress()).orElse(null);
    }

    @Override
    public VoiceProxyConfig getConfig() {
        return this.voiceProxyConfig;
    }

    @Override
    public VoiceChatLogger getLogger() {
        return this.voiceChatLogger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.proxyServer.getChannelRegistrar().register(MinecraftChannelIdentifier.from("vc:secret"), MinecraftChannelIdentifier.from("voicechat:secret"));
        this.reloadConfig();
        this.reloadVoiceProxyServer();
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        this.reloadConfig();
        this.reloadVoiceProxyServer();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (this.voiceProxyServer != null) this.voiceProxyServer.interrupt();
    }

    /**
     * This handler detects whether a player has switched servers and if so,
     * disconnects the current VoiceProxyBridge and resets the sniffer.
     */
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getPreviousServer().isEmpty()) return;
        this.onPlayerServerDisconnected(event.getPlayer().getUniqueId());
    }

    /**
     * This handler detects when a player has disconnected from the velocity proxy and if so,
     * disconnects the current VoiceProxyBridge and resets the sniffer.
     */
    @Subscribe
    public void onPlayerDisconnected(DisconnectEvent event) {
        this.onPlayerServerDisconnected(event.getPlayer().getUniqueId());
    }

    /**
     * This handler is used to intercept plugin messages between the client and backend server
     * so the proxy is able to sniff the SecretPacket.
     */
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        Player p = null;
        if (event.getSource() instanceof Player) p = (Player) event.getSource();
        if (event.getTarget() instanceof Player) p = (Player) event.getTarget();
        if (p == null) return;
        this.voiceProxySniffer.onPluginMessage(event.getIdentifier().getId(), ByteBuffer.wrap(event.getData()), p.getUniqueId());
    }
    private void reloadConfig() {
        try {
            Files.createDirectories(dataDirectory);
            Path configPath = dataDirectory.resolve("config.toml");
            voiceProxyConfig = VelocityVoiceProxyConfig.read(proxyServer, configPath);
        } catch (Exception e) {
            this.voiceChatLogger.error("An error prevented SimpleVoiceChat-Velocity to load correctly", e);
        }
    }
}
