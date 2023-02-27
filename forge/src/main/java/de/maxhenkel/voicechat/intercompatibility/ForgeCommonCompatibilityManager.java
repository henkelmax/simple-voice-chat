package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.events.ServerVoiceChatConnectedEvent;
import de.maxhenkel.voicechat.events.ServerVoiceChatDisconnectedEvent;
import de.maxhenkel.voicechat.events.VoiceChatCompatibilityCheckSucceededEvent;
import de.maxhenkel.voicechat.net.ForgeNetManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.permission.ForgePermissionManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fmlserverevents.FMLServerStartedEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ForgeCommonCompatibilityManager extends CommonCompatibilityManager {

    private final List<Consumer<MinecraftServer>> serverStartingEvents;
    private final List<Consumer<MinecraftServer>> serverStoppingEvents;
    private final List<Consumer<CommandDispatcher<CommandSourceStack>>> registerServerCommandsEvents;
    private final List<Consumer<ServerPlayer>> playerLoggedInEvents;
    private final List<Consumer<ServerPlayer>> playerLoggedOutEvents;
    private final List<Consumer<ServerPlayer>> voicechatConnectEvents;
    private final List<Consumer<ServerPlayer>> voicechatCompatibilityCheckSucceededEvents;
    private final List<Consumer<UUID>> voicechatDisconnectEvents;

    public ForgeCommonCompatibilityManager() {
        serverStartingEvents = new ArrayList<>();
        serverStoppingEvents = new ArrayList<>();
        registerServerCommandsEvents = new ArrayList<>();
        playerLoggedInEvents = new ArrayList<>();
        playerLoggedOutEvents = new ArrayList<>();
        voicechatConnectEvents = new ArrayList<>();
        voicechatCompatibilityCheckSucceededEvents = new ArrayList<>();
        voicechatDisconnectEvents = new ArrayList<>();
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartedEvent event) {
        serverStartingEvents.forEach(consumer -> consumer.accept(event.getServer()));
    }

    @SubscribeEvent
    public void serverStopping(FMLServerStoppingEvent event) {
        serverStoppingEvents.forEach(consumer -> consumer.accept(event.getServer()));
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        registerServerCommandsEvents.forEach(consumer -> consumer.accept(event.getDispatcher()));
    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            playerLoggedInEvents.forEach(consumer -> consumer.accept(player));
        }
    }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            playerLoggedOutEvents.forEach(consumer -> consumer.accept(player));
        }
    }

    @Override
    public String getModVersion() {
        return ModList.get().getModFileById(Voicechat.MODID).versionString();
    }

    @Override
    public String getModName() {
        return ModList.get().getMods().stream().filter(info -> info.getModId().equals(Voicechat.MODID)).findAny().map(IModInfo::getDisplayName).orElse(Voicechat.MODID);
    }

    @Override
    public Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public void emitServerVoiceChatConnectedEvent(ServerPlayer player) {
        voicechatConnectEvents.forEach(consumer -> consumer.accept(player));
        MinecraftForge.EVENT_BUS.post(new ServerVoiceChatConnectedEvent(player));
    }

    @Override
    public void emitServerVoiceChatDisconnectedEvent(UUID clientID) {
        voicechatDisconnectEvents.forEach(consumer -> consumer.accept(clientID));
        MinecraftForge.EVENT_BUS.post(new ServerVoiceChatDisconnectedEvent(clientID));
    }

    @Override
    public void emitPlayerCompatibilityCheckSucceeded(ServerPlayer player) {
        voicechatCompatibilityCheckSucceededEvents.forEach(consumer -> consumer.accept(player));
        MinecraftForge.EVENT_BUS.post(new VoiceChatCompatibilityCheckSucceededEvent(player));
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
    public void onPlayerCompatibilityCheckSucceeded(Consumer<ServerPlayer> onPlayerCompatibilityCheckSucceeded) {
        voicechatCompatibilityCheckSucceededEvents.add(onPlayerCompatibilityCheckSucceeded);
    }

    @Override
    public void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSourceStack>> onRegisterServerCommands) {
        registerServerCommandsEvents.add(onRegisterServerCommands);
    }

    private ForgeNetManager netManager;

    @Override
    public NetManager getNetManager() {
        if (netManager == null) {
            netManager = new ForgeNetManager();
        }
        return netManager;
    }

    @Override
    public boolean isDevEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isDedicatedServer() {
        return FMLLoader.getDist().isDedicatedServer();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public List<VoicechatPlugin> loadPlugins() {
        List<VoicechatPlugin> plugins = new ArrayList<>();
        ModList.get().getAllScanData().forEach(scan -> {
            scan.getAnnotations().forEach(annotationData -> {
                if (annotationData.annotationType().getClassName().equals(ForgeVoicechatPlugin.class.getName())) {
                    try {
                        Class<?> clazz = Class.forName(annotationData.memberName());
                        if (VoicechatPlugin.class.isAssignableFrom(clazz)) {
                            VoicechatPlugin plugin = (VoicechatPlugin) clazz.getDeclaredConstructor().newInstance();
                            plugins.add(plugin);
                        }
                    } catch (Exception e) {
                        Voicechat.LOGGER.warn("Failed to load plugin '{}': {}", annotationData.memberName(), e.getMessage());
                    }
                }
            });
        });
        return plugins;
    }

    @Override
    public PermissionManager createPermissionManager() {
        return new ForgePermissionManager();
    }
}
