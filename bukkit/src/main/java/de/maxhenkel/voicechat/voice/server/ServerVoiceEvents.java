package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.RequestSecretPacket;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerVoiceEvents implements Listener {

    private final Map<UUID, Integer> clientCompatibilities;
    private final Server server;

    public ServerVoiceEvents(org.bukkit.Server mcServer) {
        clientCompatibilities = new ConcurrentHashMap<>();

        if (!mcServer.getOnlineMode()) {
            Voicechat.LOGGER.warn("Running in offline mode - Voice chat encryption is not secure!");
        }

        server = new Server(mcServer);
        server.start();
        PluginManager.instance().onServerStarted(mcServer);
    }

    public void onRequestSecretPacket(Player player, RequestSecretPacket packet) {
        Voicechat.LOGGER.info("Received secret request of {} ({})", player.getName(), packet.getCompatibilityVersion());

        UUID playerUUID;

        try {
            playerUUID = player.getUniqueId();
        } catch (UnsupportedOperationException e) {
            player.kickPlayer("Tried to authenticate voice chat while still connecting");
            Voicechat.LOGGER.warn("{} tried to authenticate voice chat while still connecting", player.getName());
            return;
        }

        clientCompatibilities.put(playerUUID, packet.getCompatibilityVersion());
        if (packet.getCompatibilityVersion() != Voicechat.COMPATIBILITY_VERSION) {
            Voicechat.LOGGER.warn("Connected client {} has incompatible voice chat version (server={}, client={})", player.getName(), Voicechat.COMPATIBILITY_VERSION, packet.getCompatibilityVersion());
            NetManager.sendMessage(player, getIncompatibleMessage(packet.getCompatibilityVersion()));
        } else {
            initializePlayerConnection(player);
        }
    }

    public boolean isCompatible(Player player) {
        return clientCompatibilities.getOrDefault(player.getUniqueId(), -1) == Voicechat.COMPATIBILITY_VERSION;
    }

    public static Component getIncompatibleMessage(int clientCompatibilityVersion) {
        if (clientCompatibilityVersion <= 6) {
            // Send a literal string, as we don't know if the translations exist on these versions
            return Component.text(String.format(Voicechat.translate("not_compatible"), Voicechat.INSTANCE.getDescription().getVersion(), "Simple Voice Chat"));
        } else {
            // This translation key is only available for compatibility version 7+
            return Component.translatable("message.voicechat.incompatible_version",
                    Component.text(Voicechat.INSTANCE.getDescription().getVersion()).toBuilder().decorate(TextDecoration.BOLD).build(),
                    Component.text("Simple Voice Chat").toBuilder().decorate(TextDecoration.BOLD).build());
        }
    }

    public void initializePlayerConnection(Player player) {
        if (server == null) {
            return;
        }
        server.getPlayerStateManager().onPlayerCompatibilityCheckSucceeded(player);

        if (!player.hasPermission(PermissionManager.CONNECT_PERMISSION)) {
            Voicechat.LOGGER.info("Player {} has no permission to connect to the voice chat", player.getName());
            return;
        }

        UUID secret = server.getSecret(player.getUniqueId());
        NetManager.sendToClient(player, new SecretPacket(player, secret, Voicechat.SERVER_CONFIG));
        Voicechat.LOGGER.info("Sent secret to {}", player.getName());
    }

    @EventHandler
    public void playerLoggedIn(PlayerLoginEvent event) {
        if (!Voicechat.SERVER_CONFIG.forceVoiceChat.get()) {
            return;
        }
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(Voicechat.INSTANCE, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (!isCompatible(player)) {
                player.kickPlayer("You need %s %s to play on this server".formatted(
                        Voicechat.INSTANCE.getDescription().getName(),
                        Voicechat.INSTANCE.getDescription().getVersion()
                ));
            }
        }, Voicechat.SERVER_CONFIG.loginTimeout.get() / 50L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clientCompatibilities.remove(event.getPlayer().getUniqueId());
        if (server == null) {
            return;
        }

        server.disconnectClient(event.getPlayer().getUniqueId());
        Voicechat.LOGGER.info("Disconnecting client {}", event.getPlayer().getName());
    }

    public Server getServer() {
        return server;
    }
}
