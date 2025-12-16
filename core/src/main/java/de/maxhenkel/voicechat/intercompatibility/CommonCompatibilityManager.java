package de.maxhenkel.voicechat.intercompatibility;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.service.Service;
import io.netty.buffer.ByteBuf;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class CommonCompatibilityManager {

    public static CommonCompatibilityManager INSTANCE = Service.get(CommonCompatibilityManager.class);

    public abstract String getModVersion();

    public abstract String getModName();

    public abstract Path getGameDirectory();

    public abstract void emitServerVoiceChatConnectedEvent(ServerPlayer player);

    public abstract void emitServerVoiceChatDisconnectedEvent(UUID clientID);

    public abstract void emitPlayerCompatibilityCheckSucceeded(ServerPlayer player);

    public abstract void onServerVoiceChatConnected(Consumer<ServerPlayer> onVoiceChatConnected);

    public abstract void onServerVoiceChatDisconnected(Consumer<UUID> onVoiceChatDisconnected);

    public abstract void onServerStarting(Consumer<MinecraftServer> onServerStarting);

    public abstract void onServerStopping(Consumer<MinecraftServer> onServerStopping);

    public abstract void onPlayerLoggedIn(Consumer<ServerPlayer> onPlayerLoggedIn);

    public abstract void onPlayerLoggedOut(Consumer<ServerPlayer> onPlayerLoggedOut);

    /**
     * @param onPlayerHide (visibilityChangedPlayer, observingPlayer)
     */
    public abstract void onPlayerHide(BiConsumer<ServerPlayer, ServerPlayer> onPlayerHide);

    /**
     * @param onPlayerShow (visibilityChangedPlayer, observingPlayer)
     */
    public abstract void onPlayerShow(BiConsumer<ServerPlayer, ServerPlayer> onPlayerShow);

    public abstract void onPlayerCompatibilityCheckSucceeded(Consumer<ServerPlayer> onPlayerCompatibilityCheckSucceeded);

    public abstract void registerCommands();

    public abstract NetManager getNetManager();

    public abstract boolean isDevEnvironment();

    public abstract boolean isDedicatedServer();

    public abstract boolean isModLoaded(String modId);

    public abstract List<VoicechatPlugin> loadPlugins();

    public abstract PermissionManager createPermissionManager();

    public abstract VoicechatServerApi getServerApi();

    public abstract boolean canSee(ServerPlayer player, ServerPlayer other);

    public abstract void executeOnMinecraftServer(MinecraftServer server, Runnable runnable);

    public abstract void sendMinecraftPacket(ServerPlayer player, String id, ByteBuf buffer);

    public abstract void displayClientMessage(ServerPlayer player, String message, boolean overlay);

    public abstract void createTimeoutTimer(ServerPlayer player);

    public abstract void sendIncompatibleMessage(ServerPlayer serverPlayer, int compatibilityVersion);

    public abstract Position createPosition(double x, double y, double z);
}
