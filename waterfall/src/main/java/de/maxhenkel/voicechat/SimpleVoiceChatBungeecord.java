package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.logging.JavaLoggingLogger;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.PluginMessage;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.UUID;

public class SimpleVoiceChatBungeecord extends VoiceProxy implements Listener {

    private final Plugin plugin;

    public SimpleVoiceChatBungeecord(Plugin plugin) {
        super(new JavaLoggingLogger(plugin.getLogger()));
        this.plugin = plugin;
    }

    @Override
    public InetSocketAddress getDefaultBackendSocket(UUID playerUUID) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(playerUUID);
        if (player == null) {
            return null;
        }

        Server server = player.getServer();
        if (server == null) {
            return null;
        }

        SocketAddress socketAddress = server.getSocketAddress();
        if (socketAddress instanceof InetSocketAddress) {
            return (InetSocketAddress) socketAddress;
        }

        getLogger().error("Cannot get socket of server {} because its not using ip:port to connect", server.getInfo().getName());
        return null;
    }

    @Override
    public InetSocketAddress getDefaultBindSocket() {
        if (this.plugin.getProxy().getConfig().getListeners().isEmpty()) {
            getLogger().error("Cannot evaluate default socket because Bungeecord is not configured to listen on any port.");
            return null;
        }
        ListenerInfo listenerInfo = this.plugin.getProxy().getConfig().getListeners().stream().toList().get(0);
        if (listenerInfo.getSocketAddress() instanceof InetSocketAddress) {
            return (InetSocketAddress) listenerInfo.getSocketAddress();
        }

        getLogger().error("Cannot evaluate default socket because Bungeecord is not listening on an ip:port");
        return null;
    }

    @Override
    public Path getDataDirectory() {
        return this.plugin.getDataFolder().toPath();
    }

    public void onProxyInitialization() {
        plugin.getProxy().registerChannel("vc:secret");
        plugin.getProxy().registerChannel("voicechat:secret");
        this.reloadVoiceProxyServer();
    }

    public void onProxyShutdown() {
        if (this.voiceProxyServer != null) this.voiceProxyServer.interrupt();
        plugin.getProxy().unregisterChannel("vc:secret");
        plugin.getProxy().unregisterChannel("voicechat:secret");
    }

    /**
     * VoiceProxyServer does not support config reloads, we can treat a reload like a full plugin reload
     */
    @EventHandler
    public void onProxyReload(ProxyReloadEvent event) {
        this.onProxyShutdown();
        this.onProxyInitialization();
    }

    /**
     * This handler detects whether a player has switched servers and if so,
     * disconnects the current VoiceProxyBridge and resets the sniffer.
     */
    @EventHandler
    public void onServerConnected(ServerSwitchEvent event) {
        this.onPlayerServerDisconnected(event.getPlayer().getUniqueId());
    }

    /**
     * This handler detects when a player has disconnected from the bungeecord proxy and if so,
     * disconnects the current VoiceProxyBridge and resets the sniffer.
     */
    @EventHandler
    public void onPlayerDisconnected(PlayerDisconnectEvent event) {
        this.onPlayerServerDisconnected(event.getPlayer().getUniqueId());
    }

    /**
     * This handler is used to intercept plugin messages between the client and backend server
     * so the proxy is able to sniff the SecretPacket.
     */
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        ProxiedPlayer p = null;
        if (event.getSender() instanceof ProxiedPlayer) p = (ProxiedPlayer) event.getSender();
        if (event.getReceiver() instanceof ProxiedPlayer) p = (ProxiedPlayer) event.getReceiver();
        if (p == null) return;
        ByteBuffer replacement = this.voiceProxySniffer.onPluginMessage(event.getTag(), ByteBuffer.wrap(event.getData()), p.getUniqueId());
        if (replacement == null) return;

        event.setCancelled(true);
        event.getReceiver().unsafe().sendPacket(new PluginMessage(event.getTag(), replacement.array(), true));
    }
}
