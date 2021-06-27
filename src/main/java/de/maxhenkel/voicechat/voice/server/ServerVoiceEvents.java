package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.command.VoiceChatCommands;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.net.InitPacket;
import de.maxhenkel.voicechat.net.NetManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ServerVoiceEvents implements Listener {

    private Server server;

    public ServerVoiceEvents(org.bukkit.Server mcServer) {
        server = new Server(Voicechat.SERVER_CONFIG.voiceChatPort.get(), mcServer);
        server.start();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        initializePlayerConnection(event.getPlayer());
    }

    public void initializePlayerConnection(Player player) {
        if (server == null) {
            return;
        }

        if (!player.hasPermission(VoiceChatCommands.CONNECT_PERMISSION)) {
            return;
        }

        UUID secret = server.getSecret(player.getUniqueId());

        boolean hasGroupPermission = player.hasPermission(VoiceChatCommands.GROUPS_PERMISSION);

        NetManager.sendToClient(player, new InitPacket(secret, Voicechat.SERVER_CONFIG.voiceChatPort.get(), (ServerConfig.Codec) Voicechat.SERVER_CONFIG.voiceChatCodec.get(), Voicechat.SERVER_CONFIG.voiceChatMtuSize.get(), Voicechat.SERVER_CONFIG.voiceChatDistance.get(), Voicechat.SERVER_CONFIG.voiceChatFadeDistance.get(), Voicechat.SERVER_CONFIG.keepAlive.get(), Voicechat.SERVER_CONFIG.groupsEnabled.get() && hasGroupPermission, Voicechat.SERVER_CONFIG.voiceHost.get()));
        Voicechat.LOGGER.info("Sent secret to " + player.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerLoggedOut(event.getPlayer());
    }

    public void playerLoggedOut(Player player) {
        if (server == null) {
            return;
        }

        server.disconnectClient(player.getUniqueId());
        Voicechat.LOGGER.info("Disconnecting client " + player.getName());
    }

    public Server getServer() {
        return server;
    }
}
