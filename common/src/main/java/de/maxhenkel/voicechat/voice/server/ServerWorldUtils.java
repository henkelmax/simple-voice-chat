package de.maxhenkel.voicechat.voice.server;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ServerWorldUtils {

    public static Collection<ServerPlayer> getPlayersInRange(ServerLevel level, Vec3 pos, double range, Predicate<ServerPlayer> filter) {
        List<ServerPlayer> nearbyPlayers = new ArrayList<>();
        List<ServerPlayer> players = level.players();
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = players.get(i);
            if (isInRange(player.position(), pos, range) && filter.test(player)) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    public static boolean isInRange(Vec3 pos1, Vec3 pos2, double range) {
        return Math.abs(pos1.x - pos2.x) <= range && Math.abs(pos1.y - pos2.y) <= range && Math.abs(pos1.z - pos2.z) <= range;
    }

}
