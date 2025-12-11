package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.intercompatibility.CrossSideManager;
import de.maxhenkel.voicechat.intercompatibility.UncommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.Secret;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerVoiceEvents {

    public final Map<UUID, Integer> clientCompatibilities;
    private Server server;

    public ServerVoiceEvents() {
        clientCompatibilities = new ConcurrentHashMap<>();
        UncommonCompatibilityManager.INSTANCE.onServerStarting(this::serverStarting);
        UncommonCompatibilityManager.INSTANCE.onPlayerLoggedIn(this::playerLoggedIn);
        UncommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(this::playerLoggedOut);
        UncommonCompatibilityManager.INSTANCE.onPlayerHide(this::onPlayerHide);
        UncommonCompatibilityManager.INSTANCE.onPlayerShow(this::onPlayerShow);
        UncommonCompatibilityManager.INSTANCE.onServerStopping(this::serverStopping);

        UncommonCompatibilityManager.INSTANCE.onServerVoiceChatConnected(this::serverVoiceChatConnected);
        UncommonCompatibilityManager.INSTANCE.onServerVoiceChatDisconnected(this::serverVoiceChatDisconnected);
        UncommonCompatibilityManager.INSTANCE.onPlayerCompatibilityCheckSucceeded(this::playerCompatibilityCheckSucceeded);

        Voicechat.outSourcing.setServerListener(this);
    }

    public boolean isCompatible(de.maxhenkel.voicechat.api.ServerPlayer player) {
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

    public void initializePlayerConnection(de.maxhenkel.voicechat.api.ServerPlayer player) {
        if (server == null) {
            return;
        }
        UncommonCompatibilityManager.INSTANCE.emitPlayerCompatibilityCheckSucceeded(player);

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

        Timer timer = new Timer("%s-login-timer".formatted(serverPlayer.getName()), true);
        timer.schedule(Voicechat.outSourcing.getTimerSchedule(this, timer, server, serverPlayer), Voicechat.SERVER_CONFIG.loginTimeout.get());
    }

    public void playerLoggedOut(de.maxhenkel.voicechat.api.ServerPlayer player) {
        clientCompatibilities.remove(player.getUuid());
        if (server == null) {
            return;
        }

        server.onPlayerLoggedOut(player);
        Voicechat.LOGGER.info("Disconnecting client {}", player.getName());
    }

    public void onPlayerHide(de.maxhenkel.voicechat.api.ServerPlayer visibilityChangedPlayer, de.maxhenkel.voicechat.api.ServerPlayer observingPlayer) {
        if (server == null) {
            return;
        }

        server.onPlayerHide(visibilityChangedPlayer, observingPlayer);
    }

    public void onPlayerShow(de.maxhenkel.voicechat.api.ServerPlayer visibilityChangedPlayer, de.maxhenkel.voicechat.api.ServerPlayer observingPlayer) {
        if (server == null) {
            return;
        }

        server.onPlayerShow(visibilityChangedPlayer, observingPlayer);
    }

    public void serverVoiceChatConnected(de.maxhenkel.voicechat.api.ServerPlayer serverPlayer) {
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

    public void playerCompatibilityCheckSucceeded(de.maxhenkel.voicechat.api.ServerPlayer player) {
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
