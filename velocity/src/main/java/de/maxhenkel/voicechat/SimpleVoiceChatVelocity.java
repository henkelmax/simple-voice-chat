package de.maxhenkel.voicechat;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
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
import de.maxhenkel.voicechat.command.CommandSender;
import de.maxhenkel.voicechat.logging.JavaLoggingLogger;
import de.maxhenkel.voicechat.sniffer.IncompatibleVoiceChatException;
import de.maxhenkel.voicechat.integration.viaversion.ViaVersionCompatibility;
import de.maxhenkel.voicechat.util.BackendServer;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(
        id = SimpleVoiceChatVelocity.MOD_ID,
        name = "Simple Voice Chat",
        version = VoiceProxy.MOD_VERSION,
        authors = {"NilaTheDragon", "Max Henkel"},
        url = "https://github.com/henkelmax/simple-voice-chat",
        description = "Run multiple servers with Simple Voice Chat behind a single public port"
)
public class SimpleVoiceChatVelocity extends VoiceProxy {

    @DataDirectory
    @Inject
    private Path dataDirectory;
    @Inject
    private ProxyServer proxyServer;

    @Inject
    public SimpleVoiceChatVelocity(Logger logger) {
        super(new JavaLoggingLogger(logger));
    }

    @Override
    public InetSocketAddress getDefaultBackendSocket(UUID playerUUID) {
        Optional<Player> player = proxyServer.getPlayer(playerUUID);
        if (player.isEmpty()) {
            return null;
        }

        Optional<ServerConnection> server = player.get().getCurrentServer();
        if (server.isEmpty()) {
            return null;
        }

        InetSocketAddress serverSocket = server.get().getServerInfo().getAddress();
        if (serverSocket.isUnresolved()) {
            serverSocket = new InetSocketAddress(serverSocket.getHostString(), serverSocket.getPort());
        }
        return serverSocket;
    }

    @Override
    public InetSocketAddress getDefaultBindSocket() {
        return proxyServer.getBoundAddress();
    }

    @Override
    public Path getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public List<BackendServer> getBackendServers() {
        return proxyServer.getAllServers().stream().map(server -> new BackendServer(server.getServerInfo().getName(), server.getServerInfo().getAddress())).toList();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxyServer.getChannelRegistrar().register(
                MinecraftChannelIdentifier.from(REQUEST_SECRET_CHANNEL),
                MinecraftChannelIdentifier.from(REQUEST_SECRET_CHANNEL_1_12),
                MinecraftChannelIdentifier.from(SECRET_CHANNEL),
                MinecraftChannelIdentifier.from(SECRET_CHANNEL_1_12)
        );
        proxyServer.getCommandManager().register(proxyServer.getCommandManager().metaBuilder(VOICECHAT_COMMAND).plugin(this).build(), (SimpleCommand) invocation -> {
            onVoicechatCommand(new CommandSender() {
                @Override
                public void sendMessage(String message) {
                    invocation.source().sendMessage(Component.text(message));
                }

                @Override
                public boolean hasPermission(String permission) {
                    return invocation.source().hasPermission(permission);
                }
            }, invocation.arguments());
        });
        initViaVersionIntegration();
        reloadVoiceProxyServer();
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        initViaVersionIntegration();
        reloadVoiceProxyServer();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (voiceProxyServer != null) {
            voiceProxyServer.interrupt();
        }
    }

    /**
     * This handler detects whether a player has switched servers and if so,
     * disconnects the current VoiceProxyBridge and resets the sniffer.
     */
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getPreviousServer().isEmpty()) {
            return;
        }
        onPlayerServerDisconnected(event.getPlayer().getUniqueId());
    }

    /**
     * This handler detects when a player has disconnected from the velocity proxy and if so,
     * disconnects the current VoiceProxyBridge and resets the sniffer.
     */
    @Subscribe
    public void onPlayerDisconnected(DisconnectEvent event) {
        if (event.getLoginStatus().equals(DisconnectEvent.LoginStatus.CONFLICTING_LOGIN)) {
            return;
        }
        onPlayerServerDisconnected(event.getPlayer().getUniqueId());
    }

    /**
     * This handler is used to intercept plugin messages between the client and backend server
     * so the proxy is able to sniff the SecretPacket.
     */
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        Player p = null;
        if (event.getSource() instanceof Player) {
            p = (Player) event.getSource();
        }
        if (event.getTarget() instanceof Player) {
            p = (Player) event.getTarget();
        }
        if (p == null) {
            return;
        }

        try {
            ByteBuffer replacement = voiceProxySniffer.onPluginMessage(event.getIdentifier().getId(), ByteBuffer.wrap(event.getData()), p.getUniqueId());
            if (replacement == null) {
                return;
            }
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            event.getTarget().sendPluginMessage(event.getIdentifier(), replacement.array());
        } catch (IncompatibleVoiceChatException e) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            getLogger().info("Player {} has an incompatible voice chat version: {}", p.getUsername(), e.getMessage());
        }
    }

    private void initViaVersionIntegration() {
        try {
            if (proxyServer.getPluginManager().getPlugin("viaversion").isPresent()) {
                ViaVersionCompatibility.register();
                getLogger().info("Successfully added ViaVersion mappings");
            }
        } catch (Throwable t) {
            getLogger().error("Failed to add ViaVersion mappings", t);
        }
    }
}
