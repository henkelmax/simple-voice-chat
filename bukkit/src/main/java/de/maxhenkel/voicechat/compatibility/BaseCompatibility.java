package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
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
    public NamespacedKey createNamespacedKey(String key) {
        return new NamespacedKey(Voicechat.MODID, key);
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
}
