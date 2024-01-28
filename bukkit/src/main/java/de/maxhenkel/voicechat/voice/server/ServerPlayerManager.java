package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class ServerPlayerManager implements Listener {

    public static final ServerPlayerManager INSTANCE = new ServerPlayerManager();

    public static void init(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(ServerPlayerManager.INSTANCE, plugin);
        Voicechat.compatibility.scheduleSyncRepeatingTask(INSTANCE::refresh, 0, 20 * 30);
    }

    private final Set<Player> players;

    private ServerPlayerManager() {
        players = new HashSet<>();
    }

    private void refresh() {
        synchronized (players) {
            players.clear();
            players.addAll(Bukkit.getOnlinePlayers());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        synchronized (players) {
            players.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        synchronized (players) {
            players.remove(event.getPlayer());
        }
    }

    public static ArrayList<Player> getPlayersInRange(World level, Location pos, double range, @Nullable Predicate<Player> filter) {
        return INSTANCE.getPlayersInRangeInternal(level, pos, range, filter);
    }

    private ArrayList<Player> getPlayersInRangeInternal(World world, Location pos, double range, @Nullable Predicate<Player> filter) {
        ArrayList<Player> nearbyPlayers = new ArrayList<>();
        synchronized (players) {
            for (Player player : players) {
                if (!world.equals(player.getWorld())) {
                    continue;
                }
                if (isInRange(player.getLocation(), pos, range) && (filter == null || filter.test(player))) {
                    nearbyPlayers.add(player);
                }
            }
        }
        return nearbyPlayers;
    }

    public static boolean isInRange(Location pos1, Location pos2, double range) {
        return (square(pos1.getX() - pos2.getX()) + square(pos1.getY() - pos2.getY()) + square(pos1.getZ() - pos2.getZ())) <= square(range);
    }

    private static double square(double value) {
        return value * value;
    }

}
