package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.RequestSecretPacket;
import de.maxhenkel.voicechat.net.SecretPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;

public class ServerVoiceEvents {

    private Server server;

    public ServerVoiceEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarting);
        PlayerEvents.PLAYER_LOGGED_OUT.register(this::playerLoggedOut);

        NetManager.registerServerReceiver(RequestSecretPacket.class, (server, player, handler, responseSender, packet) -> {
            Voicechat.LOGGER.info("Received secret request of {}", player.getDisplayName().getString());
            if (packet.getCompatibilityVersion() != Voicechat.COMPATIBILITY_VERSION) {
                Voicechat.LOGGER.warn("Connected client {} has incompatible voice chat version (server={}, client={})", player.getName().getString(), Voicechat.COMPATIBILITY_VERSION, packet.getCompatibilityVersion());
                handler.disconnect(Voicechat.getIncompatibleMessage(packet.getCompatibilityVersion()));
                return;
            }
            initializePlayerConnection(player);
        });
    }

    public void serverStarting(MinecraftServer mcServer) {
        if (server != null) {
            server.close();
            server = null;
        }
        if (mcServer instanceof DedicatedServer) {
            try {
                server = new Server(Voicechat.SERVER_CONFIG.voiceChatPort.get(), mcServer);
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initializePlayerConnection(ServerPlayer player) {
        if (server == null) {
            return;
        }

        UUID secret = server.getSecret(player.getUUID());
        NetManager.sendToClient(player, new SecretPacket(secret, Voicechat.SERVER_CONFIG));
        Voicechat.LOGGER.info("Sent secret to " + player.getDisplayName().getString());
    }

    public void playerLoggedOut(ServerPlayer player) {
        if (server == null) {
            return;
        }

        server.disconnectClient(player.getUUID());
        Voicechat.LOGGER.info("Disconnecting client " + player.getDisplayName().getString());
    }

    @Nullable
    public Server getServer() {
        return server;
    }
}
