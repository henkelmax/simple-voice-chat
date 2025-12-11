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
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ForgeCommonCompatibilityManager extends CommonCompatibilityManager {

    private final List<Consumer<de.maxhenkel.voicechat.api.MinecraftServer>> serverStartingEvents;
    private final List<Consumer<de.maxhenkel.voicechat.api.MinecraftServer>> serverStoppingEvents;
    private final List<Consumer<CommandDispatcher<CommandSourceStack>>> registerServerCommandsEvents;
    private final List<Consumer<de.maxhenkel.voicechat.api.ServerPlayer>> playerLoggedInEvents;
    private final List<Consumer<de.maxhenkel.voicechat.api.ServerPlayer>> playerLoggedOutEvents;
    private final List<Consumer<de.maxhenkel.voicechat.api.ServerPlayer>> voicechatConnectEvents;
    private final List<Consumer<de.maxhenkel.voicechat.api.ServerPlayer>> voicechatCompatibilityCheckSucceededEvents;
    private final List<Consumer<UUID>> voicechatDisconnectEvents;

    public ForgeCommonCompatibilityManager() {
        serverStartingEvents = new CopyOnWriteArrayList<>();
        serverStoppingEvents = new CopyOnWriteArrayList<>();
        registerServerCommandsEvents = new CopyOnWriteArrayList<>();
        playerLoggedInEvents = new CopyOnWriteArrayList<>();
        playerLoggedOutEvents = new CopyOnWriteArrayList<>();
        voicechatConnectEvents = new CopyOnWriteArrayList<>();
        voicechatCompatibilityCheckSucceededEvents = new CopyOnWriteArrayList<>();
        voicechatDisconnectEvents = new CopyOnWriteArrayList<>();
    }

    @SubscribeEvent
    public void serverStarting(ServerStartedEvent event) {
        serverStartingEvents.forEach(consumer -> consumer.accept(Voicechat.outSourcing.getServerApi().fromServer(event.getServer())));
    }

    @SubscribeEvent
    public void serverStopping(ServerStoppingEvent event) {
        serverStoppingEvents.forEach(consumer -> consumer.accept(Voicechat.outSourcing.getServerApi().fromServer(event.getServer())));
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        registerServerCommandsEvents.forEach(consumer -> consumer.accept(event.getDispatcher()));
    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            playerLoggedInEvents.forEach(consumer -> consumer.accept(Voicechat.outSourcing.getServerApi().fromServerPlayer(player)));
        }
    }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            playerLoggedOutEvents.forEach(consumer -> consumer.accept(Voicechat.outSourcing.getServerApi().fromServerPlayer(player)));
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
    public void emitServerVoiceChatConnectedEvent(de.maxhenkel.voicechat.api.ServerPlayer player) {
        voicechatConnectEvents.forEach(consumer -> consumer.accept(player));
        MinecraftForge.EVENT_BUS.post(new ServerVoiceChatConnectedEvent((ServerPlayer)player.getPlayer()));
    }

    @Override
    public void emitServerVoiceChatDisconnectedEvent(UUID clientID) {
        voicechatDisconnectEvents.forEach(consumer -> consumer.accept(clientID));
        MinecraftForge.EVENT_BUS.post(new ServerVoiceChatDisconnectedEvent(clientID));
    }

    @Override
    public void emitPlayerCompatibilityCheckSucceeded(de.maxhenkel.voicechat.api.ServerPlayer player) {
        voicechatCompatibilityCheckSucceededEvents.forEach(consumer -> consumer.accept(player));
        MinecraftForge.EVENT_BUS.post(new VoiceChatCompatibilityCheckSucceededEvent((ServerPlayer)player.getPlayer()));
    }

    @Override
    public void onServerVoiceChatConnected(Consumer<de.maxhenkel.voicechat.api.ServerPlayer> onVoiceChatConnected) {
        voicechatConnectEvents.add(onVoiceChatConnected);
    }

    @Override
    public void onServerVoiceChatDisconnected(Consumer<UUID> onVoiceChatDisconnected) {
        voicechatDisconnectEvents.add(onVoiceChatDisconnected);
    }

    @Override
    public void onServerStarting(Consumer<de.maxhenkel.voicechat.api.MinecraftServer> onServerStarting) {
        serverStartingEvents.add(onServerStarting);
    }

    @Override
    public void onServerStopping(Consumer<de.maxhenkel.voicechat.api.MinecraftServer> onServerStopping) {
        serverStoppingEvents.add(onServerStopping);
    }

    @Override
    public void onPlayerLoggedIn(Consumer<de.maxhenkel.voicechat.api.ServerPlayer> onPlayerLoggedIn) {
        playerLoggedInEvents.add(onPlayerLoggedIn);
    }

    @Override
    public void onPlayerLoggedOut(Consumer<de.maxhenkel.voicechat.api.ServerPlayer> onPlayerLoggedOut) {
        playerLoggedOutEvents.add(onPlayerLoggedOut);
    }

    @Override
    public void onPlayerHide(BiConsumer<de.maxhenkel.voicechat.api.ServerPlayer, de.maxhenkel.voicechat.api.ServerPlayer> onPlayerHide) {
        // Do nothing for now
    }

    @Override
    public void onPlayerShow(BiConsumer<de.maxhenkel.voicechat.api.ServerPlayer, de.maxhenkel.voicechat.api.ServerPlayer> onPlayerShow) {
        // Do nothing for now
    }

    @Override
    public void onPlayerCompatibilityCheckSucceeded(Consumer<de.maxhenkel.voicechat.api.ServerPlayer> onPlayerCompatibilityCheckSucceeded) {
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
                    } catch (Throwable e) {
                        Voicechat.LOGGER.warn("Failed to load plugin '{}'", annotationData.memberName(), e);
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

    @Override
    public boolean canSee(de.maxhenkel.voicechat.api.ServerPlayer player, de.maxhenkel.voicechat.api.ServerPlayer other) {
        return true;
    }
}
