package de.maxhenkel.voicechat.intercompatibility;

import de.maxhenkel.voicechat.ForgeVoicechatMod;
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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ForgeCommonCompatibilityManager extends CommonCompatibilityManager {

    private final List<Consumer<MinecraftServer>> serverStartingEvents;
    private final List<Consumer<MinecraftServer>> serverStoppingEvents;
    private final List<Consumer<EntityPlayerMP>> playerLoggedInEvents;
    private final List<Consumer<EntityPlayerMP>> playerLoggedOutEvents;
    private final List<Consumer<EntityPlayerMP>> voicechatConnectEvents;
    private final List<Consumer<EntityPlayerMP>> voicechatCompatibilityCheckSucceededEvents;
    private final List<Consumer<UUID>> voicechatDisconnectEvents;
    private ASMDataTable asmDataTable;

    public ForgeCommonCompatibilityManager() {
        serverStartingEvents = new ArrayList<>();
        serverStoppingEvents = new ArrayList<>();
        playerLoggedInEvents = new ArrayList<>();
        playerLoggedOutEvents = new ArrayList<>();
        voicechatConnectEvents = new ArrayList<>();
        voicechatCompatibilityCheckSucceededEvents = new ArrayList<>();
        voicechatDisconnectEvents = new ArrayList<>();
    }

    public void preInit(FMLPreInitializationEvent event) {
        asmDataTable = event.getAsmData();
    }

    public void serverStarted(FMLServerStartedEvent event) {
        serverStartingEvents.forEach(consumer -> consumer.accept(FMLCommonHandler.instance().getMinecraftServerInstance()));
    }

    public void serverStopping(FMLServerStoppingEvent event) {
        serverStoppingEvents.forEach(consumer -> consumer.accept(FMLCommonHandler.instance().getMinecraftServerInstance()));
    }

    @SubscribeEvent
    public void playerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            playerLoggedInEvents.forEach(consumer -> consumer.accept(player));
        }
    }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            playerLoggedOutEvents.forEach(consumer -> consumer.accept(player));
        }
    }

    @Override
    public String getModVersion() {
        ModContainer modContainer = Loader.instance().activeModContainer();
        if (modContainer == null) {
            return "N/A";
        }
        return modContainer.getVersion();
    }

    @Override
    public String getModName() {
        ModContainer modContainer = Loader.instance().activeModContainer();
        if (modContainer == null) {
            return "N/A";
        }
        return modContainer.getName();
    }

    @Override
    public Path getGameDirectory() {
        return Loader.instance().getConfigDir().toPath().getParent();
    }

    @Override
    public void emitServerVoiceChatConnectedEvent(EntityPlayerMP player) {
        voicechatConnectEvents.forEach(consumer -> consumer.accept(player));
        MinecraftForge.EVENT_BUS.post(new ServerVoiceChatConnectedEvent(player));
    }

    @Override
    public void emitServerVoiceChatDisconnectedEvent(UUID clientID) {
        voicechatDisconnectEvents.forEach(consumer -> consumer.accept(clientID));
        MinecraftForge.EVENT_BUS.post(new ServerVoiceChatDisconnectedEvent(clientID));
    }

    @Override
    public void emitPlayerCompatibilityCheckSucceeded(EntityPlayerMP player) {
        voicechatCompatibilityCheckSucceededEvents.forEach(consumer -> consumer.accept(player));
        MinecraftForge.EVENT_BUS.post(new VoiceChatCompatibilityCheckSucceededEvent(player));
    }

    @Override
    public void onServerVoiceChatConnected(Consumer<EntityPlayerMP> onVoiceChatConnected) {
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
    public void onPlayerLoggedIn(Consumer<EntityPlayerMP> onPlayerLoggedIn) {
        playerLoggedInEvents.add(onPlayerLoggedIn);
    }

    @Override
    public void onPlayerLoggedOut(Consumer<EntityPlayerMP> onPlayerLoggedOut) {
        playerLoggedOutEvents.add(onPlayerLoggedOut);
    }

    @Override
    public void onPlayerCompatibilityCheckSucceeded(Consumer<EntityPlayerMP> onPlayerCompatibilityCheckSucceeded) {
        voicechatCompatibilityCheckSucceededEvents.add(onPlayerCompatibilityCheckSucceeded);
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
        return !FMLCommonHandler.instance().findContainerFor(ForgeVoicechatMod.INSTANCE).getSource().isFile();
    }

    @Override
    public boolean isDedicatedServer() {
        try {
            Class.forName("net.minecraft.client.Minecraft");
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public List<VoicechatPlugin> loadPlugins() {
        List<VoicechatPlugin> plugins = new ArrayList<>();
        String annotationClassName = ForgeVoicechatPlugin.class.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Class<?> clazz = Class.forName(asmData.getClassName());
                if (VoicechatPlugin.class.isAssignableFrom(clazz)) {
                    VoicechatPlugin plugin = (VoicechatPlugin) clazz.getDeclaredConstructor().newInstance();
                    plugins.add(plugin);
                }
            } catch (Exception e) {
                Voicechat.LOGGER.warn("Failed to load plugin '{}'", asmData.getClassName(), e);
            }
        }
        return plugins;
    }

    @Override
    public PermissionManager createPermissionManager() {
        return new ForgePermissionManager();
    }
}
