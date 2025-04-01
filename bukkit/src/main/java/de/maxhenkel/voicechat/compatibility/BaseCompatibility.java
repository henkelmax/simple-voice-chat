package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public abstract class BaseCompatibility implements Compatibility {

    @Override
    public void addChannel(Player player, String channel) {
        callMethod(player, "addChannel", new Class[]{String.class}, channel);
    }

    @Override
    public void removeChannel(Player player, String channel) {
        callMethod(player, "removeChannel", new Class[]{String.class}, channel);
    }

    @Override
    public Key createNamespacedKey(String key) {
        return Key.of(key);
    }

    @Override
    public void sendTranslationMessage(Player player, String key, String... args) {
        sendJsonMessage(player, createTranslationMessage(key, args));
    }

    @Override
    public void sendStatusMessage(Player player, String key, String... args) {
        sendJsonStatusMessage(player, createTranslationMessage(key, args));
    }

    @Override
    public void runTask(Runnable runnable) {
        if (doesMethodExist(Bukkit.class, "getGlobalRegionScheduler")) {
            Object globalRegionScheduler = callMethod(Bukkit.class, "getGlobalRegionScheduler");
            callMethod(globalRegionScheduler, "run", new Class[]{Plugin.class, Consumer.class}, Voicechat.INSTANCE, (Consumer<?>) (task) -> runnable.run());
            return;
        }
        Bukkit.getScheduler().runTask(Voicechat.INSTANCE, runnable);
    }

    @Override
    public void scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
        if (doesMethodExist(Bukkit.class, "getGlobalRegionScheduler")) {
            Object globalRegionScheduler = callMethod(Bukkit.class, "getGlobalRegionScheduler");
            if (doesMethodExist(globalRegionScheduler.getClass(), "runAtFixedRate")) {
                callMethod(globalRegionScheduler, "runAtFixedRate", new Class[]{Plugin.class, Consumer.class, Long.class, Long.class}, Voicechat.INSTANCE, (Consumer<?>) (task) -> runnable.run(), delay, period);
            }
            return;
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Voicechat.INSTANCE, runnable, delay, period);
    }

    @Override
    public String getBaseBukkitPackage() {
        return Bukkit.getServer().getClass().getPackage().getName();
    }

    @Override
    public String getBaseServerPackage() {
        return callMethod(Bukkit.getServer(), "getServer").getClass().getPackage().getName();
    }
}
