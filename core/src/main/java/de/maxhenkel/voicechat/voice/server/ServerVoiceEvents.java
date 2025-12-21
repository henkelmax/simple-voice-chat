package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CrossSideManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.Secret;
import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Map;
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
        CommonCompatibilityManager.INSTANCE.onPlayerHide(this::onPlayerHide);
        CommonCompatibilityManager.INSTANCE.onPlayerShow(this::onPlayerShow);
        CommonCompatibilityManager.INSTANCE.onServerStopping(this::serverStopping);

        CommonCompatibilityManager.INSTANCE.onServerVoiceChatConnected(this::serverVoiceChatConnected);
        CommonCompatibilityManager.INSTANCE.onServerVoiceChatDisconnected(this::serverVoiceChatDisconnected);
        CommonCompatibilityManager.INSTANCE.onPlayerCompatibilityCheckSucceeded(this::playerCompatibilityCheckSucceeded);

        CommonCompatibilityManager.INSTANCE.getNetManager().requestSecretChannel.setServerListener((server, player, handler, packet) -> {
            Voicechat.LOGGER.info("Received secret request of {} ({})", player.getName(), packet.getCompatibilityVersion());
            clientCompatibilities.put(player.getUuid(), packet.getCompatibilityVersion());
            if (packet.getCompatibilityVersion() != Voicechat.COMPATIBILITY_VERSION) {
                Voicechat.LOGGER.warn("Connected client {} has incompatible voice chat version (server={}, client={})", player.getName(), Voicechat.COMPATIBILITY_VERSION, packet.getCompatibilityVersion());
                CommonCompatibilityManager.INSTANCE.sendIncompatibleMessage(player, packet.getCompatibilityVersion());
            } else {
                initializePlayerConnection(player);
            }
        });
    }

    public boolean isCompatible(ServerPlayer player) {
        return isCompatible(player.getUuid());
    }

    public boolean isCompatible(UUID playerUuid) {
        return clientCompatibilities.getOrDefault(playerUuid, -1) == Voicechat.COMPATIBILITY_VERSION;
    }

    public void serverStarting(MinecraftServer mcServer) {
        if (server != null) {
            server.close();
            server = null;
        }

        if (!CrossSideManager.get().shouldRunVoiceChatServer(mcServer)) {
            Voicechat.LOGGER.info("Disabling voice chat in singleplayer");
            return;
        }

        if (mcServer.isDedicated()) {
            if (!mcServer.usesAuthentication()) {
                Voicechat.LOGGER.warn("Running in offline mode - Voice chat encryption is not secure!");
            }
        }

        try {
            server = new Server(mcServer);
            server.start();
            PluginManager.instance().onServerStarted();
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to start voice chat server", e);
        }
    }

    public void initializePlayerConnection(ServerPlayer player) {
        if (server == null) {
            return;
        }
        CommonCompatibilityManager.INSTANCE.emitPlayerCompatibilityCheckSucceeded(player);

        Secret secret = server.generateNewSecret(player.getUuid());
        if (secret == null) {
            Voicechat.LOGGER.warn("Player already requested secret - ignoring");
            return;
        }
        NetManager.sendToClient(player, new SecretPacket(player, secret, server.getPort(), Voicechat.SERVER_CONFIG));
        Voicechat.LOGGER.info("Sent secret to {}", player.getName());
    }

    public void playerLoggedIn(ServerPlayer serverPlayer) {
        if (server != null) {
            server.onPlayerLoggedIn(serverPlayer);
        }

        if (!Voicechat.SERVER_CONFIG.forceVoiceChat.get()) {
            return;
        }

        CommonCompatibilityManager.INSTANCE.createTimeoutTimer(serverPlayer);
    }

    public void playerLoggedOut(ServerPlayer player) {
        clientCompatibilities.remove(player.getUuid());
        if (server == null) {
            return;
        }

        server.onPlayerLoggedOut(player);
        Voicechat.LOGGER.info("Disconnecting client {}", player.getName());
    }

    public void onPlayerHide(ServerPlayer visibilityChangedPlayer, ServerPlayer observingPlayer) {
        if (server == null) {
            return;
        }

        server.onPlayerHide(visibilityChangedPlayer, observingPlayer);
    }

    public void onPlayerShow(ServerPlayer visibilityChangedPlayer, ServerPlayer observingPlayer) {
        if (server == null) {
            return;
        }

        server.onPlayerShow(visibilityChangedPlayer, observingPlayer);
    }

    public void serverVoiceChatConnected(ServerPlayer serverPlayer) {
        if (server == null) {
            return;
        }

        server.onPlayerVoicechatConnect(serverPlayer);
    }

    public void serverVoiceChatDisconnected(UUID uuid) {
        if (server == null) {
            return;
        }

        server.onPlayerVoicechatDisconnect(uuid);
    }

    public void playerCompatibilityCheckSucceeded(ServerPlayer player) {
        if (server == null) {
            return;
        }

        server.onPlayerCompatibilityCheckSucceeded(player);
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
