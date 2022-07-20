package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.net.NetManager;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.service.Service;
import net.minecraft.server.MinecraftServer;

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

    public abstract void onPlayerCompatibilityCheckSucceeded(Consumer<ServerPlayerEntity> onPlayerCompatibilityCheckSucceeded);

    public abstract void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSource>> onRegisterServerCommands);

    public abstract NetManager getNetManager();

    public abstract String listLoadedMods();

    public abstract String listKeybinds();

    public abstract boolean isDevEnvironment();

    public abstract boolean isDedicatedServer();

    public abstract List<VoicechatPlugin> loadPlugins();

    public abstract PermissionManager createPermissionManager();

}
