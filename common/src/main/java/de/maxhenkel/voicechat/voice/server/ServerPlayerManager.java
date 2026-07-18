package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;

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
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedIn(INSTANCE::onPlayerLoggedIn);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(INSTANCE::onPlayerLoggedOut);
    }

    private volatile Set<UUID> players;

    private ServerPlayerManager() {
        players = Collections.emptySet();
    }

    private synchronized void onPlayerLoggedIn(ServerPlayer player) {
        Set<UUID> newPlayers = getOnlinePlayers(player);
        newPlayers.add(player.getUUID());
        players = newPlayers;
    }

    private synchronized void onPlayerLoggedOut(ServerPlayer player) {
        Set<UUID> newPlayers = getOnlinePlayers(player);
        newPlayers.remove(player.getUUID());
        players = newPlayers;
    }

    private static Set<UUID> getOnlinePlayers(ServerPlayer player) {
        Set<UUID> onlinePlayers = new HashSet<>();
        for (ServerPlayer onlinePlayer : player.level().getServer().getPlayerList().getPlayers()) {
            onlinePlayers.add(onlinePlayer.getUUID());
        }
        return onlinePlayers;
    }

    public static Collection<ServerPlayer> getPlayersInRange(ServerLevel level, Vec3 pos, double range, @Nullable Predicate<ServerPlayer> filter) {
        return INSTANCE.getPlayersInRangeInternal(level, pos, range, filter);
    }

    private Collection<ServerPlayer> getPlayersInRangeInternal(ServerLevel level, Vec3 pos, double range, @Nullable Predicate<ServerPlayer> filter) {
        List<ServerPlayer> nearbyPlayers = new ArrayList<>();
        PlayerList playerList = level.getServer().getPlayerList();
        for (UUID uuid : players) {
            ServerPlayer player = playerList.getPlayer(uuid);
            if (player == null || player.level() != level) {
                continue;
            }
            if (isInRange(player.position(), pos, range) && (filter == null || filter.test(player))) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    public static boolean isInRange(Vec3 pos1, Vec3 pos2, double range) {
        return pos1.distanceToSqr(pos2) <= range * range;
    }

}
