package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.impl.VoicechatServerApiImpl;
import de.maxhenkel.voicechat.service.Service;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

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

    public abstract void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSourceStack>> onRegisterServerCommands);

    public abstract NetManager getNetManager();

    public abstract boolean isDevEnvironment();

    public abstract boolean isDedicatedServer();

    public abstract boolean isModLoaded(String modId);

    public abstract List<VoicechatPlugin> loadPlugins();

    public abstract PermissionManager createPermissionManager();

    public VoicechatServerApi getServerApi() {
        return VoicechatServerApiImpl.INSTANCE;
    }

    public Object createRawApiEntity(Entity entity) {
        return entity;
    }

    public Object createRawApiPlayer(Player player) {
        return player;
    }

    public Object createRawApiLevel(ServerLevel level) {
        return level;
    }

    public abstract boolean canSee(ServerPlayer player, ServerPlayer other);

    public void execute(MinecraftServer server, Runnable runnable) {
        server.execute(runnable);
    }

}
