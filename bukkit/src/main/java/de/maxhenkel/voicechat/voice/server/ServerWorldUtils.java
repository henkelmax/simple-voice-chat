package de.maxhenkel.voicechat.voice.server;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ServerWorldUtils {

    public static Collection<Player> getPlayersInRange(World level, Location pos, double range, Predicate<Player> filter) {
        List<Player> nearbyPlayers = new ArrayList<>();
        List<Player> players = level.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (isInRange(player.getLocation(), pos, range) && filter.test(player)) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    public static boolean isInRange(Location pos1, Location pos2, double range) {
        return Math.abs(pos1.getX() - pos2.getX()) <= range && Math.abs(pos1.getY() - pos2.getY()) <= range && Math.abs(pos1.getZ() - pos2.getZ()) <= range;
    }

}
