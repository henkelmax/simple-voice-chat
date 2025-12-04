package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.dialstuff.MinimalLevel;
import de.maxhenkel.voicechat.dialstuff.MinimalPlayer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ServerWorldUtils {

    public static Collection<ServerPlayer> getPlayersInRange(ServerLevel level, Position pos, double range, @Nullable Predicate<ServerPlayer> filter) {
        List<ServerPlayer> nearbyPlayers = new ArrayList<>();
        List<ServerPlayer> players = level.players();
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = players.get(i);
            if (isInRange(player.getPosition(), pos, range) && (filter == null || filter.test(player))) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    public static boolean isInRange(Position pos1, Position pos2, double range) {
        return pos1.distanceToSqr(pos2) <= range * range;
    }

}
