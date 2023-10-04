package de.maxhenkel.voicechat.voice.server;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ServerWorldUtils {

    public static Collection<ServerPlayerEntity> getPlayersInRange(ServerWorld level, Vector3d pos, double range, Predicate<ServerPlayerEntity> filter) {
        List<ServerPlayerEntity> nearbyPlayers = new ArrayList<>();
        List<ServerPlayerEntity> players = level.players();
        for (int i = 0; i < players.size(); i++) {
            ServerPlayerEntity player = players.get(i);
            if (isInRange(player.position(), pos, range) && filter.test(player)) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    public static boolean isInRange(Vector3d pos1, Vector3d pos2, double range) {
        return pos1.distanceToSqr(pos2) <= range * range;
    }

}
