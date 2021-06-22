package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.net.InitPacket;
import de.maxhenkel.voicechat.net.NetManager;
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
        PlayerEvents.PLAYER_LOGGED_IN.register(this::initializePlayerConnection);
        PlayerEvents.PLAYER_LOGGED_OUT.register(this::playerLoggedOut);
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
        NetManager.sendToClient(player, new InitPacket(secret, Voicechat.SERVER_CONFIG.voiceChatPort.get(), (ServerConfig.Codec) Voicechat.SERVER_CONFIG.voiceChatCodec.get(), Voicechat.SERVER_CONFIG.voiceChatMtuSize.get(), Voicechat.SERVER_CONFIG.voiceChatDistance.get(), Voicechat.SERVER_CONFIG.voiceChatFadeDistance.get(), Voicechat.SERVER_CONFIG.keepAlive.get(), Voicechat.SERVER_CONFIG.groupsEnabled.get(), Voicechat.SERVER_CONFIG.voiceHost.get()));
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
