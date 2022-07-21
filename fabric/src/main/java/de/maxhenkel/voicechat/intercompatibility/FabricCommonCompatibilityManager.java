package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.events.ServerVoiceChatEvents;
import de.maxhenkel.voicechat.net.FabricNetManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.permission.FabricPermissionManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FabricCommonCompatibilityManager extends CommonCompatibilityManager {

    @Override
    public String getModVersion() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(Voicechat.MODID).orElse(null);
        if (modContainer == null) {
            return "N/A";
        }
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public String getModName() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(Voicechat.MODID).orElse(null);
        if (modContainer == null) {
            return Voicechat.MODID;
        }
        return modContainer.getMetadata().getName();
    }

    @Override
    public Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public void emitServerVoiceChatConnectedEvent(ServerPlayer player) {
        ServerVoiceChatEvents.VOICECHAT_CONNECTED.invoker().accept(player);
    }

    @Override
    public void emitServerVoiceChatDisconnectedEvent(UUID clientID) {
        ServerVoiceChatEvents.VOICECHAT_DISCONNECTED.invoker().accept(clientID);
    }

    @Override
    public void emitPlayerCompatibilityCheckSucceeded(ServerPlayer player) {
        ServerVoiceChatEvents.VOICECHAT_COMPATIBILITY_CHECK_SUCCEEDED.invoker().accept(player);
    }

    @Override
    public void onServerVoiceChatConnected(Consumer<ServerPlayer> onVoiceChatConnected) {
        ServerVoiceChatEvents.VOICECHAT_CONNECTED.register(onVoiceChatConnected);
    }

    @Override
    public void onServerVoiceChatDisconnected(Consumer<UUID> onVoiceChatDisconnected) {
        ServerVoiceChatEvents.VOICECHAT_DISCONNECTED.register(onVoiceChatDisconnected);
    }

    @Override
    public void onServerStarting(Consumer<MinecraftServer> onServerStarting) {
        ServerLifecycleEvents.SERVER_STARTED.register(onServerStarting::accept);
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> onServerStopping) {
        ServerLifecycleEvents.SERVER_STOPPING.register(onServerStopping::accept);
    }

    @Override
    public void onPlayerLoggedIn(Consumer<ServerPlayer> onPlayerLoggedIn) {
        PlayerEvents.PLAYER_LOGGED_IN.register(onPlayerLoggedIn);
    }

    @Override
    public void onPlayerLoggedOut(Consumer<ServerPlayer> onPlayerLoggedOut) {
        PlayerEvents.PLAYER_LOGGED_OUT.register(onPlayerLoggedOut);
    }

    @Override
    public void onPlayerCompatibilityCheckSucceeded(Consumer<ServerPlayer> onPlayerCompatibilityCheckSucceeded) {
        ServerVoiceChatEvents.VOICECHAT_COMPATIBILITY_CHECK_SUCCEEDED.register(onPlayerCompatibilityCheckSucceeded);
    }

    @Override
    public void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSourceStack>> onRegisterServerCommands) {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> onRegisterServerCommands.accept(dispatcher));
    }

    private FabricNetManager netManager;

    @Override
    public NetManager getNetManager() {
        if (netManager == null) {
            netManager = new FabricNetManager();
        }
        return netManager;
    }

    @Override
    public boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isDedicatedServer() {
        return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.SERVER);
    }

    @Override
    public List<VoicechatPlugin> loadPlugins() {
        return FabricLoader.getInstance().getEntrypointContainers(Voicechat.MODID, VoicechatPlugin.class).stream().map(EntrypointContainer::getEntrypoint).collect(Collectors.toList());
    }

    @Override
    public PermissionManager createPermissionManager() {
        return new FabricPermissionManager();
    }

}
