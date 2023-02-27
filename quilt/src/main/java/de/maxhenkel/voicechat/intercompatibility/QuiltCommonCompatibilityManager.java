package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.events.ServerVoiceChatEvents;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.QuiltNetManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.permission.QuiltPermissionManager;
import net.fabricmc.api.EnvType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointContainer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QuiltCommonCompatibilityManager extends CommonCompatibilityManager {

    @Override
    public String getModVersion() {
        ModContainer modContainer = QuiltLoader.getModContainer(Voicechat.MODID).orElse(null);
        if (modContainer == null) {
            return "N/A";
        }
        return modContainer.metadata().version().raw();
    }

    @Override
    public String getModName() {
        ModContainer modContainer = QuiltLoader.getModContainer(Voicechat.MODID).orElse(null);
        if (modContainer == null) {
            return Voicechat.MODID;
        }
        return modContainer.metadata().name();
    }

    @Override
    public Path getGameDirectory() {
        return QuiltLoader.getGameDir();
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
        ServerLifecycleEvents.READY.register(onServerStarting::accept);
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> onServerStopping) {
        ServerLifecycleEvents.STOPPING.register(onServerStopping::accept);
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
        CommandRegistrationCallback.EVENT.register((dispatcher, integrated, dedicated) -> onRegisterServerCommands.accept(dispatcher));
    }

    private QuiltNetManager netManager;

    @Override
    public NetManager getNetManager() {
        if (netManager == null) {
            netManager = new QuiltNetManager();
        }
        return netManager;
    }

    @Override
    public boolean isDevEnvironment() {
        return QuiltLoader.isDevelopmentEnvironment();
    }

    @Override
    public boolean isDedicatedServer() {
        return MinecraftQuiltLoader.getEnvironmentType().equals(EnvType.SERVER);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return QuiltLoader.isModLoaded(modId);
    }

    @Override
    public List<VoicechatPlugin> loadPlugins() {
        return QuiltLoader.getEntrypointContainers(Voicechat.MODID, VoicechatPlugin.class).stream().map(EntrypointContainer::getEntrypoint).collect(Collectors.toList());
    }

    @Override
    public PermissionManager createPermissionManager() {
        return new QuiltPermissionManager();
    }

}
