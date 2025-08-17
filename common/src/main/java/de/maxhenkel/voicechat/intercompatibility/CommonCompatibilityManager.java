package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.net.NetManager;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.impl.VoicechatServerApiImpl;
import de.maxhenkel.voicechat.service.Service;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class CommonCompatibilityManager {

    public static CommonCompatibilityManager INSTANCE = Service.get(CommonCompatibilityManager.class);

    public abstract String getModVersion();

    public abstract String getModName();

    public abstract Path getGameDirectory();

    public abstract void emitServerVoiceChatConnectedEvent(ServerPlayerEntity player);

    public abstract void emitServerVoiceChatDisconnectedEvent(UUID clientID);

    public abstract void emitPlayerCompatibilityCheckSucceeded(ServerPlayerEntity player);

    public abstract void onServerVoiceChatConnected(Consumer<ServerPlayerEntity> onVoiceChatConnected);

    public abstract void onServerVoiceChatDisconnected(Consumer<UUID> onVoiceChatDisconnected);

    public abstract void onServerStarting(Consumer<MinecraftServer> onServerStarting);

    public abstract void onServerStopping(Consumer<MinecraftServer> onServerStopping);

    public abstract void onPlayerLoggedIn(Consumer<ServerPlayerEntity> onPlayerLoggedIn);

    public abstract void onPlayerLoggedOut(Consumer<ServerPlayerEntity> onPlayerLoggedOut);

    public abstract void onPlayerHide(Consumer<PlayerVisibilityEvent> onPlayerHide);

    public abstract void onPlayerShow(Consumer<PlayerVisibilityEvent> onPlayerShow);

    public abstract void onPlayerCompatibilityCheckSucceeded(Consumer<ServerPlayerEntity> onPlayerCompatibilityCheckSucceeded);

    public abstract void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSource>> onRegisterServerCommands);

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

    public Object createRawApiPlayer(PlayerEntity player) {
        return player;
    }

    public Object createRawApiLevel(ServerWorld level) {
        return level;
    }

    public abstract boolean canSee(ServerPlayer player, ServerPlayer other);

    public static class PlayerVisibilityEvent {

        private final ServerPlayer visibilityChangedPlayer;
        private final ServerPlayer observingPlayer;

        public PlayerVisibilityEvent(ServerPlayer visibilityChangedPlayer, ServerPlayer observingPlayer) {
            this.visibilityChangedPlayer = visibilityChangedPlayer;
            this.observingPlayer = observingPlayer;
        }

        public ServerPlayer getVisibilityChangedPlayer() {
            return visibilityChangedPlayer;
        }

        public ServerPlayer getObservingPlayer() {
            return observingPlayer;
        }
    }

}
