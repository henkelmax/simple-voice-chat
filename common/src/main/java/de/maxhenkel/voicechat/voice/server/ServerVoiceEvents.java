package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.SecretPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;

public class ServerVoiceEvents {

    private Server server;

    public ServerVoiceEvents() {
        CommonCompatibilityManager.INSTANCE.onServerStarting(this::serverStarting);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(this::playerLoggedOut);

        CommonCompatibilityManager.INSTANCE.getNetManager().requestSecretChannel.registerServerListener((server, player, handler, packet) -> {
            Voicechat.LOGGER.info("Received secret request of {}", player.getDisplayName().getString());
            if (packet.getCompatibilityVersion() != Voicechat.COMPATIBILITY_VERSION) {
                Voicechat.LOGGER.warn("Connected client {} has incompatible voice chat version (server={}, client={})", player.getName().getString(), Voicechat.COMPATIBILITY_VERSION, packet.getCompatibilityVersion());
                handler.disconnect(getIncompatibleMessage(packet.getCompatibilityVersion()));
                return;
            }
            initializePlayerConnection(player);
        });
    }

    public Component getIncompatibleMessage(int clientCompatibilityVersion) {
        if (clientCompatibilityVersion <= 6) {
            return new TextComponent("Your voice chat version is not compatible with the servers version.\nPlease install version ")
                    .append(new TextComponent(CommonCompatibilityManager.INSTANCE.getModVersion()).withStyle(ChatFormatting.BOLD))
                    .append(" of ")
                    .append(new TextComponent(CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD))
                    .append(".");
        } else {
            return new TranslatableComponent("message.voicechat.incompatible_version",
                    new TextComponent(CommonCompatibilityManager.INSTANCE.getModVersion()).withStyle(ChatFormatting.BOLD),
                    new TextComponent(CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD));
        }
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
        CommonCompatibilityManager.INSTANCE.getNetManager().sendToClient(player, new SecretPacket(secret, Voicechat.SERVER_CONFIG));
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
