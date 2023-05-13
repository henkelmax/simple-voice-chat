package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.plugins.PluginManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerVoiceEvents {

    private final Map<UUID, Integer> clientCompatibilities;
    private Server server;

    public ServerVoiceEvents() {
        clientCompatibilities = new ConcurrentHashMap<>();
        CommonCompatibilityManager.INSTANCE.onServerStarting(this::serverStarting);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedIn(this::playerLoggedIn);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(this::playerLoggedOut);
        CommonCompatibilityManager.INSTANCE.onServerStopping(this::serverStopping);

        CommonCompatibilityManager.INSTANCE.getNetManager().requestSecretChannel.setServerListener((server, player, handler, packet) -> {
            Voicechat.LOGGER.info("Received secret request of {} ({})", player.getDisplayName().getString(), packet.getCompatibilityVersion());
            clientCompatibilities.put(player.getUUID(), packet.getCompatibilityVersion());
            if (packet.getCompatibilityVersion() != Voicechat.COMPATIBILITY_VERSION) {
                Voicechat.LOGGER.warn("Connected client {} has incompatible voice chat version (server={}, client={})", player.getName().getString(), Voicechat.COMPATIBILITY_VERSION, packet.getCompatibilityVersion());
                player.sendMessage(getIncompatibleMessage(packet.getCompatibilityVersion()), Util.NIL_UUID);
            } else {
                initializePlayerConnection(player);
            }
        });
    }

    public ITextComponent getIncompatibleMessage(int clientCompatibilityVersion) {
        if (clientCompatibilityVersion <= 6) {
            return new StringTextComponent(String.format(Voicechat.TRANSLATIONS.voicechatNotCompatibleMessage.get(), CommonCompatibilityManager.INSTANCE.getModVersion(), CommonCompatibilityManager.INSTANCE.getModName()));
        } else {
            return new TranslationTextComponent("message.voicechat.incompatible_version",
                    new StringTextComponent(CommonCompatibilityManager.INSTANCE.getModVersion()).withStyle(TextFormatting.BOLD),
                    new StringTextComponent(CommonCompatibilityManager.INSTANCE.getModName()).withStyle(TextFormatting.BOLD));
        }
    }

    public boolean isCompatible(ServerPlayerEntity player) {
        return isCompatible(player.getUUID());
    }

    public boolean isCompatible(UUID playerUuid) {
        return clientCompatibilities.getOrDefault(playerUuid, -1) == Voicechat.COMPATIBILITY_VERSION;
    }

    public void serverStarting(MinecraftServer mcServer) {
        if (server != null) {
            server.close();
            server = null;
        }

        if (!(mcServer instanceof DedicatedServer) && VoicechatClient.CLIENT_CONFIG != null && !VoicechatClient.CLIENT_CONFIG.runLocalServer.get()) {
            Voicechat.LOGGER.info("Disabling voice chat in singleplayer");
            return;
        }

        if (mcServer instanceof DedicatedServer) {
            if (!mcServer.usesAuthentication()) {
                Voicechat.LOGGER.warn("Running in offline mode - Voice chat encryption is not secure!");
            }
        }

        try {
            server = new Server(mcServer);
            server.start();
            PluginManager.instance().onServerStarted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializePlayerConnection(ServerPlayerEntity player) {
        if (server == null) {
            return;
        }
        CommonCompatibilityManager.INSTANCE.emitPlayerCompatibilityCheckSucceeded(player);

        UUID secret = server.getSecret(player.getUUID());
        NetManager.sendToClient(player, new SecretPacket(player, secret, server.getPort(), Voicechat.SERVER_CONFIG));
        Voicechat.LOGGER.info("Sent secret to {}", player.getDisplayName().getString());
    }

    public void playerLoggedIn(ServerPlayerEntity serverPlayer) {
        if (!Voicechat.SERVER_CONFIG.forceVoiceChat.get()) {
            return;
        }

        Timer timer = new Timer(serverPlayer.getGameProfile().getName() + "%s-login-timer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                timer.purge();
                if (!serverPlayer.server.isRunning()) {
                    return;
                }
                if (!serverPlayer.connection.connection.isConnected()) {
                    return;
                }
                if (!isCompatible(serverPlayer)) {
                    serverPlayer.server.execute(() -> {
                        serverPlayer.connection.disconnect(
                                new StringTextComponent(String.format(
                                        Voicechat.TRANSLATIONS.forceVoicechatKickMessage.get(),
                                        CommonCompatibilityManager.INSTANCE.getModName(),
                                        CommonCompatibilityManager.INSTANCE.getModVersion()
                                ))
                        );
                    });
                }
            }
        }, Voicechat.SERVER_CONFIG.loginTimeout.get());
    }

    public void playerLoggedOut(ServerPlayerEntity player) {
        clientCompatibilities.remove(player.getUUID());
        if (server == null) {
            return;
        }

        server.disconnectClient(player.getUUID());
        Voicechat.LOGGER.info("Disconnecting client {}", player.getDisplayName().getString());
    }

    @Nullable
    public Server getServer() {
        return server;
    }

    public void serverStopping(MinecraftServer mcServer) {
        if (server != null) {
            server.close();
            server = null;
        }
    }

}
