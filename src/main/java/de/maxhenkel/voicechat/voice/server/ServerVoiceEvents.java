package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.net.AuthenticationMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.UUID;

public class ServerVoiceEvents {

    private Server server;

    public void serverStarting(FMLServerStartedEvent event) {
        if (server != null) {
            server.close();
            server = null;
        }
        if (event.getServer() instanceof DedicatedServer) {
            try {
                server = new Server(Main.SERVER_CONFIG.voiceChatPort.get(), event.getServer());
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initializePlayerConnection(ServerPlayerEntity player) {
        if (server == null) {
            return;
        }

        server.getPlayerStateManager().onPlayerLoggedIn(player);

        UUID secret = server.getSecret(player.getUniqueID());
        Main.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new AuthenticationMessage(player.getUniqueID(), secret));
        Main.LOGGER.info("Sent secret to " + player.getDisplayName().getString());
    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            initializePlayerConnection((ServerPlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (server == null) {
            return;
        }

        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

            server.getPlayerStateManager().onPlayerLoggedOut(player);

            server.disconnectClient(player.getUniqueID());
            Main.LOGGER.info("Disconnecting client " + player.getDisplayName().getString());
        }
    }

    @Nullable
    public Server getServer() {
        return server;
    }
}
