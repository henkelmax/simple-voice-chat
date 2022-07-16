package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.service.Service;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class CommonCompatibilityManager {

    public static CommonCompatibilityManager INSTANCE = Service.get(CommonCompatibilityManager.class);

    public abstract String getModVersion();

    public abstract String getModName();

    public abstract Path getGameDirectory();

    public abstract void emitServerVoiceChatConnectedEvent(ServerPlayer player);

    public abstract void emitServerVoiceChatDisconnectedEvent(UUID clientID);

    public abstract void onServerVoiceChatConnected(Consumer<ServerPlayer> onVoiceChatConnected);

    public abstract void onServerVoiceChatDisconnected(Consumer<UUID> onVoiceChatDisconnected);

    public abstract void onServerStarting(Consumer<MinecraftServer> onServerStarting);

    public abstract void onServerStopping(Consumer<MinecraftServer> onServerStopping);

    public abstract void onPlayerLoggedIn(Consumer<ServerPlayer> onPlayerLoggedIn);

    public abstract void onPlayerLoggedOut(Consumer<ServerPlayer> onPlayerLoggedOut);

    public abstract void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSourceStack>> onRegisterServerCommands);

    public abstract NetManager getNetManager();

    public abstract String listLoadedMods();

    public abstract String listKeybinds();

    public abstract boolean isDevEnvironment();

    public abstract boolean isDedicatedServer();

    public abstract List<VoicechatPlugin> loadPlugins();

    public abstract PermissionManager createPermissionManager();

}
