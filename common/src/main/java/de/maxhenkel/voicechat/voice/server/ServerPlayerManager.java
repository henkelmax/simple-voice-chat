package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

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

    private synchronized void onPlayerLoggedIn(ServerPlayerEntity player) {
        Set<UUID> newPlayers = getOnlinePlayers(player);
        newPlayers.add(player.getUUID());
        players = newPlayers;
    }

    private synchronized void onPlayerLoggedOut(ServerPlayerEntity player) {
        Set<UUID> newPlayers = getOnlinePlayers(player);
        newPlayers.remove(player.getUUID());
        players = newPlayers;
    }

    private static Set<UUID> getOnlinePlayers(ServerPlayerEntity player) {
        Set<UUID> onlinePlayers = new HashSet<>();
        for (ServerPlayerEntity onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
            onlinePlayers.add(onlinePlayer.getUUID());
        }
        return onlinePlayers;
    }

    public static Collection<ServerPlayerEntity> getPlayersInRange(ServerWorld level, Vector3d pos, double range, @Nullable Predicate<ServerPlayerEntity> filter) {
        return INSTANCE.getPlayersInRangeInternal(level, pos, range, filter);
    }

    private Collection<ServerPlayerEntity> getPlayersInRangeInternal(ServerWorld level, Vector3d pos, double range, @Nullable Predicate<ServerPlayerEntity> filter) {
        if (!Voicechat.SERVER_CONFIG.threadedServerSupport.get()) {
            return getPlayersInRangeDirect(level, pos, range, filter);
        }
        List<ServerPlayerEntity> nearbyPlayers = new ArrayList<>();
        PlayerList playerList = level.getServer().getPlayerList();
        for (UUID uuid : players) {
            ServerPlayerEntity player = playerList.getPlayer(uuid);
            if (player == null || player.level != level) {
                continue;
            }
            if (isInRange(player.position(), pos, range) && (filter == null || filter.test(player))) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    private static Collection<ServerPlayerEntity> getPlayersInRangeDirect(ServerWorld level, Vector3d pos, double range, @Nullable Predicate<ServerPlayerEntity> filter) {
        List<ServerPlayerEntity> nearbyPlayers = new ArrayList<>();
        List<ServerPlayerEntity> levelPlayers = level.players();
        for (int i = 0; i < levelPlayers.size(); i++) {
            ServerPlayerEntity player = levelPlayers.get(i);
            if (isInRange(player.position(), pos, range) && (filter == null || filter.test(player))) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    public static boolean isInRange(Vector3d pos1, Vector3d pos2, double range) {
        return pos1.distanceToSqr(pos2) <= range * range;
    }

}
