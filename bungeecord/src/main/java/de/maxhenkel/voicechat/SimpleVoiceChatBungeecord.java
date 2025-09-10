package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.logging.JavaLoggingLogger;
import de.maxhenkel.voicechat.sniffer.IncompatibleVoiceChatException;
import de.maxhenkel.voicechat.integration.viaversion.ViaVersionCompatibility;
import de.maxhenkel.voicechat.util.BackendServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.PluginMessage;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class SimpleVoiceChatBungeecord extends VoiceProxy implements Listener {

    private final Plugin plugin;

    public SimpleVoiceChatBungeecord(Plugin plugin) {
        super(new JavaLoggingLogger(plugin.getLogger()));
        this.plugin = plugin;
    }

    @Override
    public InetSocketAddress getDefaultBackendSocket(UUID playerUUID) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(playerUUID);
        if (player == null) {
            return null;
        }

        Server server = player.getServer();
        if (server == null) {
            return null;
        }

        SocketAddress socketAddress = server.getSocketAddress();
        if (socketAddress instanceof InetSocketAddress address) {
            return address;
        }

        getLogger().error("Cannot get socket of server {} because its not using ip:port to connect", server.getInfo().getName());
        return null;
    }

    @Override
    public InetSocketAddress getDefaultBindSocket() {
        if (plugin.getProxy().getConfig().getListeners().isEmpty()) {
            getLogger().error("Cannot evaluate default socket because Bungeecord is not configured to listen on any port.");
            return null;
        }
        ListenerInfo listenerInfo = plugin.getProxy().getConfig().getListeners().stream().toList().get(0);
        if (listenerInfo.getSocketAddress() instanceof InetSocketAddress address) {
            return address;
        }

        getLogger().error("Cannot evaluate default socket because Bungeecord is not listening on an ip:port");
        return null;
    }

    @Override
    public Path getDataDirectory() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public List<BackendServer> getBackendServers() {
        return ProxyServer.getInstance().getServers().entrySet().stream().map(e -> new BackendServer(e.getKey(), e.getValue().getSocketAddress())).toList();
    }

    public void onProxyInitialization() {
        plugin.getProxy().registerChannel(REQUEST_SECRET_CHANNEL);
        plugin.getProxy().registerChannel(REQUEST_SECRET_CHANNEL_1_12);
        plugin.getProxy().registerChannel(SECRET_CHANNEL);
        plugin.getProxy().registerChannel(SECRET_CHANNEL_1_12);

        plugin.getProxy().getPluginManager().registerCommand(plugin, new Command(VOICECHAT_COMMAND) {
            @Override
            public void execute(CommandSender sender, String[] args) {
                onVoicechatCommand(new de.maxhenkel.voicechat.command.CommandSender() {
                    @Override
                    public void sendMessage(String message) {
                        sender.sendMessage(new TextComponent(message));
                    }

                    @Override
                    public boolean hasPermission(String permission) {
                        return sender.hasPermission(permission);
                    }
                }, args);
            }
        });

        initViaVersionIntegration();
        reloadVoiceProxyServer();
    }

    public void onProxyShutdown() {
        if (voiceProxyServer != null) {
            voiceProxyServer.interrupt();
        }
        plugin.getProxy().unregisterChannel(REQUEST_SECRET_CHANNEL);
        plugin.getProxy().unregisterChannel(REQUEST_SECRET_CHANNEL_1_12);
        plugin.getProxy().unregisterChannel(SECRET_CHANNEL);
        plugin.getProxy().unregisterChannel(SECRET_CHANNEL_1_12);
    }

    /**
     * VoiceProxyServer does not support config reloads, we can treat a reload like a full plugin reload.
     */
    @EventHandler
    public void onProxyReload(ProxyReloadEvent event) {
        onProxyShutdown();
        onProxyInitialization();
    }

    /**
     * This handler detects whether a player has switched servers and if so,
     * disconnects the current VoiceProxyBridge and resets the sniffer.
     */
    @EventHandler
    public void onServerConnected(ServerSwitchEvent event) {
        onPlayerServerDisconnected(event.getPlayer().getUniqueId());
    }

    /**
     * This handler detects when a player has disconnected from the bungeecord proxy and if so,
     * disconnects the current VoiceProxyBridge and resets the sniffer.
     */
    @EventHandler
    public void onPlayerDisconnected(PlayerDisconnectEvent event) {
        onPlayerServerDisconnected(event.getPlayer().getUniqueId());
    }

    /**
     * This handler is used to intercept plugin messages between the client and backend server
     * so the proxy is able to sniff the SecretPacket.
     */
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        ProxiedPlayer p = null;
        if (event.getSender() instanceof ProxiedPlayer player) {
            p = player;
        }
        if (event.getReceiver() instanceof ProxiedPlayer player) {
            p = player;
        }
        if (p == null) {
            return;
        }

        try {
            ByteBuffer replacement = voiceProxySniffer.onPluginMessage(event.getTag(), ByteBuffer.wrap(event.getData()), p.getUniqueId());
            if (replacement == null) {
                return;
            }
            event.setCancelled(true);
            event.getReceiver().unsafe().sendPacket(new PluginMessage(event.getTag(), replacement.array(), true));
        } catch (IncompatibleVoiceChatException e) {
            event.setCancelled(true);
            getLogger().info("Player {} has an incompatible voice chat version: {}", p.getName(), e.getMessage());
        }
    }

    private void initViaVersionIntegration() {
        try {
            if (plugin.getProxy().getPluginManager().getPlugin("ViaVersion") != null) {
                ViaVersionCompatibility.register();
                getLogger().info("Successfully added ViaVersion mappings");
            }
        } catch (Throwable t) {
            getLogger().error("Failed to add ViaVersion mappings", t);
        }
    }

}
