package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.BukkitUtils;
import de.maxhenkel.voicechat.VoicechatPaperPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PaperNetManager;
import de.maxhenkel.voicechat.permission.PaperPermissionManager;
import de.maxhenkel.voicechat.plugins.impl.VoicechatPaperApiImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHideEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShowEntityEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerLoadEvent;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PaperCommonCompatibilityManager extends CommonCompatibilityManager implements Listener {

    private final List<Consumer<MinecraftServer>> serverStartingEvents;
    private final List<Consumer<MinecraftServer>> serverStoppingEvents;
    private final List<Consumer<CommandDispatcher<CommandSourceStack>>> registerServerCommandsEvents;
    private final List<Consumer<ServerPlayer>> playerLoggedInEvents;
    private final List<Consumer<ServerPlayer>> playerLoggedOutEvents;
    private final List<BiConsumer<ServerPlayer, ServerPlayer>> playerHideEvents;
    private final List<BiConsumer<ServerPlayer, ServerPlayer>> playerShowEvents;
    private final List<Consumer<ServerPlayer>> voicechatConnectEvents;
    private final List<Consumer<ServerPlayer>> voicechatCompatibilityCheckSucceededEvents;
    private final List<Consumer<UUID>> voicechatDisconnectEvents;

    public PaperCommonCompatibilityManager() {
        serverStartingEvents = new CopyOnWriteArrayList<>();
        serverStoppingEvents = new CopyOnWriteArrayList<>();
        registerServerCommandsEvents = new CopyOnWriteArrayList<>();
        playerLoggedInEvents = new CopyOnWriteArrayList<>();
        playerLoggedOutEvents = new CopyOnWriteArrayList<>();
        playerHideEvents = new CopyOnWriteArrayList<>();
        playerShowEvents = new CopyOnWriteArrayList<>();
        voicechatConnectEvents = new CopyOnWriteArrayList<>();
        voicechatCompatibilityCheckSucceededEvents = new CopyOnWriteArrayList<>();
        voicechatDisconnectEvents = new CopyOnWriteArrayList<>();
    }

    @EventHandler
    public void onServerStart(ServerLoadEvent event) {
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        serverStartingEvents.forEach(consumer -> consumer.accept(server));
    }

    @EventHandler
    public void onServerStop(PluginDisableEvent event) {
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        serverStoppingEvents.forEach(consumer -> consumer.accept(server));
        if (netManager != null) {
            netManager.close();
            netManager = null;
        }
    }

    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        registerServerCommandsEvents.forEach(consumer -> consumer.accept(commandDispatcher));
    }

    @EventHandler
    public void playerLoggedIn(PlayerJoinEvent event) {
        ServerPlayer serverPlayer = BukkitUtils.getPlayer(event.getPlayer());
        playerLoggedInEvents.forEach(consumer -> consumer.accept(serverPlayer));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ServerPlayer serverPlayer = BukkitUtils.getPlayer(event.getPlayer());
        playerLoggedOutEvents.forEach(consumer -> consumer.accept(serverPlayer));
    }

    @EventHandler
    public void onPlayerHide(PlayerHideEntityEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Player)) {
            return;
        }
        ServerPlayer hiddenPlayer = BukkitUtils.getPlayer((org.bukkit.entity.Player) event.getEntity());
        ServerPlayer player = BukkitUtils.getPlayer(event.getPlayer());

        playerHideEvents.forEach(consumer -> consumer.accept(hiddenPlayer, player));
    }

    @EventHandler
    public void onPlayerShow(PlayerShowEntityEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Player)) {
            return;
        }
        ServerPlayer shownPlayer = BukkitUtils.getPlayer((org.bukkit.entity.Player) event.getEntity());
        ServerPlayer player = BukkitUtils.getPlayer(event.getPlayer());

        playerShowEvents.forEach(consumer -> consumer.accept(shownPlayer, player));
    }

    @Override
    public String getModVersion() {
        return VoicechatPaperPlugin.INSTANCE.getPluginMeta().getVersion();
    }

    @Override
    public String getModName() {
        return "Simple Voice Chat";
    }

    @Override
    public Path getGameDirectory() {
        return Bukkit.getPluginsFolder().getParentFile().toPath();
    }

    @Override
    public void emitServerVoiceChatConnectedEvent(ServerPlayer player) {
        voicechatConnectEvents.forEach(consumer -> consumer.accept(player));
    }

    @Override
    public void emitServerVoiceChatDisconnectedEvent(UUID clientID) {
        voicechatDisconnectEvents.forEach(consumer -> consumer.accept(clientID));
    }

    @Override
    public void emitPlayerCompatibilityCheckSucceeded(ServerPlayer player) {
        voicechatCompatibilityCheckSucceededEvents.forEach(consumer -> consumer.accept(player));
    }

    @Override
    public void onServerVoiceChatConnected(Consumer<ServerPlayer> onVoiceChatConnected) {
        voicechatConnectEvents.add(onVoiceChatConnected);
    }

    @Override
    public void onServerVoiceChatDisconnected(Consumer<UUID> onVoiceChatDisconnected) {
        voicechatDisconnectEvents.add(onVoiceChatDisconnected);
    }

    @Override
    public void onServerStarting(Consumer<MinecraftServer> onServerStarting) {
        serverStartingEvents.add(onServerStarting);
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> onServerStopping) {
        serverStoppingEvents.add(onServerStopping);
    }

    @Override
    public void onPlayerLoggedIn(Consumer<ServerPlayer> onPlayerLoggedIn) {
        playerLoggedInEvents.add(onPlayerLoggedIn);
    }

    @Override
    public void onPlayerLoggedOut(Consumer<ServerPlayer> onPlayerLoggedOut) {
        playerLoggedOutEvents.add(onPlayerLoggedOut);
    }

    @Override
    public void onPlayerHide(BiConsumer<ServerPlayer, ServerPlayer> onPlayerHide) {
        playerHideEvents.add(onPlayerHide);
    }

    @Override
    public void onPlayerShow(BiConsumer<ServerPlayer, ServerPlayer> onPlayerShow) {
        playerShowEvents.add(onPlayerShow);
    }

    @Override
    public void onPlayerCompatibilityCheckSucceeded(Consumer<ServerPlayer> onPlayerCompatibilityCheckSucceeded) {
        voicechatCompatibilityCheckSucceededEvents.add(onPlayerCompatibilityCheckSucceeded);
    }

    @Override
    public void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSourceStack>> onRegisterServerCommands) {
        registerServerCommandsEvents.add(onRegisterServerCommands);
    }

    private PaperNetManager netManager;

    @Override
    public NetManager getNetManager() {
        if (netManager == null) {
            netManager = new PaperNetManager();
        }
        return netManager;
    }

    @Override
    public boolean isDevEnvironment() {
        return false;
    }

    @Override
    public boolean isDedicatedServer() {
        return true;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return Bukkit.getPluginManager().isPluginEnabled(modId);
    }

    @Override
    public List<VoicechatPlugin> loadPlugins() {
        return VoicechatPaperPlugin.apiService.getPlugins();
    }

    @Override
    public PaperPermissionManager createPermissionManager() {
        return new PaperPermissionManager();
    }

    @Override
    public VoicechatServerApi getServerApi() {
        return VoicechatPaperApiImpl.PAPER_INSTANCE;
    }

    @Override
    public Object createRawApiEntity(Entity entity) {
        return entity.getBukkitEntity();
    }

    @Override
    public Object createRawApiPlayer(Player player) {
        return player.getBukkitEntity();
    }

    @Override
    public Object createRawApiLevel(ServerLevel level) {
        return level.getWorld();
    }

    @Override
    public boolean canSee(ServerPlayer player, ServerPlayer other) {
        return player.getBukkitEntity().canSee(other.getBukkitEntity());
    }

    @Override
    public void execute(MinecraftServer server, Runnable runnable) {
        Bukkit.getServer().getGlobalRegionScheduler().execute(VoicechatPaperPlugin.INSTANCE, runnable);
    }

}
