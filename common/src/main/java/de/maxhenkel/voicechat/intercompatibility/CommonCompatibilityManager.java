package de.maxhenkel.voicechat.intercompatibility;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.impl.VoicechatServerApiImpl;
import de.maxhenkel.voicechat.service.Service;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class CommonCompatibilityManager {

    public static CommonCompatibilityManager INSTANCE = Service.get(CommonCompatibilityManager.class);

    public abstract String getModVersion();

    public abstract String getModName();

    public abstract Path getGameDirectory();

    public abstract void emitServerVoiceChatConnectedEvent(EntityPlayerMP player);

    public abstract void emitServerVoiceChatDisconnectedEvent(UUID clientID);

    public abstract void emitPlayerCompatibilityCheckSucceeded(EntityPlayerMP player);

    public abstract void onServerVoiceChatConnected(Consumer<EntityPlayerMP> onVoiceChatConnected);

    public abstract void onServerVoiceChatDisconnected(Consumer<UUID> onVoiceChatDisconnected);

    public abstract void onServerStarting(Consumer<MinecraftServer> onServerStarting);

    public abstract void onServerStopping(Consumer<MinecraftServer> onServerStopping);

    public abstract void onPlayerLoggedIn(Consumer<EntityPlayerMP> onPlayerLoggedIn);

    public abstract void onPlayerLoggedOut(Consumer<EntityPlayerMP> onPlayerLoggedOut);

    /**
     * @param onPlayerHide (visibilityChangedPlayer, observingPlayer)
     */
    public abstract void onPlayerHide(BiConsumer<EntityPlayerMP, EntityPlayerMP> onPlayerHide);

    /**
     * @param onPlayerShow (visibilityChangedPlayer, observingPlayer)
     */
    public abstract void onPlayerShow(BiConsumer<EntityPlayerMP, EntityPlayerMP> onPlayerShow);

    public abstract void onPlayerCompatibilityCheckSucceeded(Consumer<EntityPlayerMP> onPlayerCompatibilityCheckSucceeded);

    public abstract NetManager getNetManager();

    public abstract boolean isDevEnvironment();

    public abstract boolean isDedicatedServer();

    public abstract List<VoicechatPlugin> loadPlugins();

    public abstract PermissionManager createPermissionManager();

    public VoicechatServerApi getServerApi() {
        return VoicechatServerApiImpl.INSTANCE;
    }

    public Object createRawApiEntity(Entity entity) {
        return entity;
    }

    public Object createRawApiPlayer(EntityPlayer player) {
        return player;
    }

    public Object createRawApiLevel(WorldServer level) {
        return level;
    }

    public abstract boolean canSee(EntityPlayerMP player, EntityPlayerMP other);

    public void execute(MinecraftServer server, Runnable runnable) {
        server.addScheduledTask(runnable);
    }

}
