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
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ForgeCommonCompatibilityManager extends CommonCompatibilityManager {

    private final List<Consumer<MinecraftServer>> serverStartingEvents;
    private final List<Consumer<MinecraftServer>> serverStoppingEvents;
    private final List<Consumer<CommandDispatcher<CommandSource>>> registerServerCommandsEvents;
    private final List<Consumer<ServerPlayerEntity>> playerLoggedInEvents;
    private final List<Consumer<ServerPlayerEntity>> playerLoggedOutEvents;
    private final List<Consumer<ServerPlayerEntity>> voicechatConnectEvents;
    private final List<Consumer<ServerPlayerEntity>> voicechatCompatibilityCheckSucceededEvents;
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
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            playerLoggedInEvents.forEach(consumer -> consumer.accept(player));
        }
    }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            playerLoggedOutEvents.forEach(consumer -> consumer.accept(player));
        }
    }

    @Override
    public String getModVersion() {
        return ModList.get().getModContainerById(Voicechat.MODID).get().getModInfo().getVersion().toString();
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
    public void emitServerVoiceChatConnectedEvent(ServerPlayerEntity player) {
        voicechatConnectEvents.forEach(consumer -> consumer.accept(player));
        MinecraftForge.EVENT_BUS.post(new ServerVoiceChatConnectedEvent(player));
    }

    @Override
    public void emitServerVoiceChatDisconnectedEvent(UUID clientID) {
        voicechatDisconnectEvents.forEach(consumer -> consumer.accept(clientID));
        MinecraftForge.EVENT_BUS.post(new ServerVoiceChatDisconnectedEvent(clientID));
    }

    @Override
    public void emitPlayerCompatibilityCheckSucceeded(ServerPlayerEntity player) {
        voicechatCompatibilityCheckSucceededEvents.forEach(consumer -> consumer.accept(player));
        MinecraftForge.EVENT_BUS.post(new VoiceChatCompatibilityCheckSucceededEvent(player));
    }

    @Override
    public void onServerVoiceChatConnected(Consumer<ServerPlayerEntity> onVoiceChatConnected) {
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
    public void onPlayerLoggedIn(Consumer<ServerPlayerEntity> onPlayerLoggedIn) {
        playerLoggedInEvents.add(onPlayerLoggedIn);
    }

    @Override
    public void onPlayerLoggedOut(Consumer<ServerPlayerEntity> onPlayerLoggedOut) {
        playerLoggedOutEvents.add(onPlayerLoggedOut);
    }

    @Override
    public void onPlayerCompatibilityCheckSucceeded(Consumer<ServerPlayerEntity> onPlayerCompatibilityCheckSucceeded) {
        voicechatCompatibilityCheckSucceededEvents.add(onPlayerCompatibilityCheckSucceeded);
    }

    @Override
    public void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSource>> onRegisterServerCommands) {
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
    public String listLoadedMods() {
        StringBuilder sb = new StringBuilder();
        for (IModInfo mod : ModList.get().getMods()) {
            sb.append("Mod ID: " + mod.getModId());
            sb.append("\n");
            sb.append("Name: " + mod.getDisplayName());
            sb.append("\n");
            sb.append("Version: " + mod.getVersion().getQualifier());
            sb.append("\n");
            sb.append("Dependencies: " + mod.getDependencies().stream().map(IModInfo.ModVersion::getModId).collect(Collectors.joining(", ")));
            sb.append("\n\n");
        }
        return sb.toString();
    }

    @Override
    public String listKeybinds() {
        StringBuilder sb = new StringBuilder();
        for (KeyBinding mapping : Minecraft.getInstance().options.keyMappings) {
            sb.append(mapping.getName() + "(" + mapping.getCategory() + "): " + mapping.getKey().getName() + " (" + mapping.getDefaultKey().getName() + ")");
            sb.append("\n");
        }
        return sb.toString();
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
    public List<VoicechatPlugin> loadPlugins() {
        List<VoicechatPlugin> plugins = new ArrayList<>();
        ModList.get().getAllScanData().forEach(scan -> {
            scan.getAnnotations().forEach(annotationData -> {
                if (annotationData.getAnnotationType().getClassName().equals(ForgeVoicechatPlugin.class.getName())) {
                    try {
                        Class<?> clazz = Class.forName(annotationData.getMemberName());
                        if (VoicechatPlugin.class.isAssignableFrom(clazz)) {
                            VoicechatPlugin plugin = (VoicechatPlugin) clazz.getDeclaredConstructor().newInstance();
                            plugins.add(plugin);
                        }
                    } catch (Exception e) {
                        Voicechat.LOGGER.warn("Failed to load plugin '{}': {}", annotationData.getMemberName(), e.getMessage());
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
