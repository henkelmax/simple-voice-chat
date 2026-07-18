package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class ServerPlayerManager {

    public static final ServerPlayerManager INSTANCE = new ServerPlayerManager();

    public static void init() {
        if (!Voicechat.SERVER_CONFIG.threadedServerSupport.get()) {
            return;
        }
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedIn(INSTANCE::onPlayerLoggedIn);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(INSTANCE::onPlayerLoggedOut);
    }

    private volatile Set<UUID> players;

    private ServerPlayerManager() {
        players = Collections.emptySet();
    }

    private synchronized void onPlayerLoggedIn(EntityPlayerMP player) {
        Set<UUID> newPlayers = getOnlinePlayers(player);
        newPlayers.add(player.getUniqueID());
        players = newPlayers;
    }

    private synchronized void onPlayerLoggedOut(EntityPlayerMP player) {
        Set<UUID> newPlayers = getOnlinePlayers(player);
        newPlayers.remove(player.getUniqueID());
        players = newPlayers;
    }

    private static Set<UUID> getOnlinePlayers(EntityPlayerMP player) {
        Set<UUID> onlinePlayers = new HashSet<>();
        for (EntityPlayerMP onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
            onlinePlayers.add(onlinePlayer.getUniqueID());
        }
        return onlinePlayers;
    }

    public static Collection<EntityPlayerMP> getPlayersInRange(WorldServer level, Vec3d pos, double range, @Nullable Predicate<EntityPlayerMP> filter) {
        return INSTANCE.getPlayersInRangeInternal(level, pos, range, filter);
    }

    private Collection<EntityPlayerMP> getPlayersInRangeInternal(WorldServer level, Vec3d pos, double range, @Nullable Predicate<EntityPlayerMP> filter) {
        if (!Voicechat.SERVER_CONFIG.threadedServerSupport.get()) {
            return getPlayersInRangeDirect(level, pos, range, filter);
        }
        List<EntityPlayerMP> nearbyPlayers = new ArrayList<>();
        PlayerList playerList = level.getMinecraftServer().getPlayerList();
        for (UUID uuid : players) {
            EntityPlayerMP player = playerList.getPlayerByUUID(uuid);
            if (player == null || player.world != level) {
                continue;
            }
            if (isInRange(player.getPositionVector(), pos, range) && (filter == null || filter.test(player))) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    private static Collection<EntityPlayerMP> getPlayersInRangeDirect(WorldServer level, Vec3d pos, double range, @Nullable Predicate<EntityPlayerMP> filter) {
        List<EntityPlayerMP> nearbyPlayers = new ArrayList<>();
        List<EntityPlayer> levelPlayers = level.playerEntities;
        for (int i = 0; i < levelPlayers.size(); i++) {
            EntityPlayerMP player = (EntityPlayerMP) levelPlayers.get(i);
            if (isInRange(player.getPositionVector(), pos, range) && (filter == null || filter.test(player))) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    public static boolean isInRange(Vec3d pos1, Vec3d pos2, double range) {
        return pos1.squareDistanceTo(pos2) <= range * range;
    }

}
