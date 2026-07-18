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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ServerPlayerManager implements Listener {

    private static final long REFRESH_INTERVAL_TICKS = 20L * 30L;

    public static final ServerPlayerManager INSTANCE = new ServerPlayerManager();

    public static void init(Plugin plugin) {
        if (!Voicechat.SERVER_CONFIG.threadedServerSupport.get()) {
            return;
        }
        Bukkit.getPluginManager().registerEvents(ServerPlayerManager.INSTANCE, plugin);
        Voicechat.compatibility.scheduleSyncRepeatingTask(INSTANCE::refresh, 0, REFRESH_INTERVAL_TICKS);
    }

    private volatile Set<Player> players;

    private ServerPlayerManager() {
        players = Collections.emptySet();
    }

    private synchronized void refresh() {
        players = new HashSet<>(Bukkit.getOnlinePlayers());
    }

    @EventHandler
    public synchronized void onPlayerJoin(PlayerJoinEvent event) {
        Set<Player> newPlayers = new HashSet<>(players);
        newPlayers.add(event.getPlayer());
        players = newPlayers;
    }

    @EventHandler
    public synchronized void onPlayerQuit(PlayerQuitEvent event) {
        Set<Player> newPlayers = new HashSet<>(players);
        newPlayers.remove(event.getPlayer());
        players = newPlayers;
    }

    public static Collection<Player> getPlayersInRange(World level, Location pos, double range, @Nullable Predicate<Player> filter) {
        return INSTANCE.getPlayersInRangeInternal(level, pos, range, filter);
    }

    private Collection<Player> getPlayersInRangeInternal(World world, Location pos, double range, @Nullable Predicate<Player> filter) {
        if (!Voicechat.SERVER_CONFIG.threadedServerSupport.get()) {
            return getPlayersInRangeDirect(world, pos, range, filter);
        }
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player player : players) {
            if (!world.equals(player.getWorld())) {
                continue;
            }
            if (isInRange(player.getLocation(), pos, range) && (filter == null || filter.test(player))) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    private static Collection<Player> getPlayersInRangeDirect(World world, Location pos, double range, @Nullable Predicate<Player> filter) {
        List<Player> nearbyPlayers = new ArrayList<>();
        List<Player> worldPlayers = world.getPlayers();
        for (int i = 0; i < worldPlayers.size(); i++) {
            Player player = worldPlayers.get(i);
            if (isInRange(player.getLocation(), pos, range) && (filter == null || filter.test(player))) {
                nearbyPlayers.add(player);
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
