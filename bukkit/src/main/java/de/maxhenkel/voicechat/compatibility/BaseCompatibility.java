package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public abstract class BaseCompatibility implements Compatibility {

    private String baseBukkitPackage;
    private String baseServerPackage;

    private Method addChannel;
    private Method removeChannel;
    private Consumer<Runnable> runTask;
    private TaskScheduler taskScheduler;

    @Override
    public void init() throws Exception {
        Compatibility.super.init();

        baseBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
        baseServerPackage = callMethod(Bukkit.getServer(), "getServer").getClass().getPackage().getName();

        Class<?> craftPlayer = getBukkitClass("entity.CraftPlayer");
        addChannel = getMethod(craftPlayer, new String[]{"addChannel"}, new Class[]{String.class});
        removeChannel = getMethod(craftPlayer, new String[]{"removeChannel"}, new Class[]{String.class});

        runTask = runnable -> Bukkit.getScheduler().runTask(Voicechat.INSTANCE, runnable);
        taskScheduler = (runnable, delay, period) -> Bukkit.getScheduler().scheduleSyncRepeatingTask(Voicechat.INSTANCE, runnable, delay, period);
        if (doesMethodExist(Bukkit.class, "getGlobalRegionScheduler")) {
            Object globalRegionScheduler = callMethod(Bukkit.class, "getGlobalRegionScheduler");

            Method run = getMethod(globalRegionScheduler.getClass(), new String[]{"run"}, new Class[]{Plugin.class, Consumer.class});
            runTask = runnable -> call(run, globalRegionScheduler, Voicechat.INSTANCE, (Consumer<?>) (task) -> runnable.run());

            if (doesMethodExist(globalRegionScheduler.getClass(), "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class)) {
                Method runAtFixedRate = getMethod(globalRegionScheduler.getClass(), new String[]{"runAtFixedRate"}, new Class[]{Plugin.class, Consumer.class, long.class, long.class});
                taskScheduler = (runnable, delay, period) -> call(runAtFixedRate, globalRegionScheduler, Voicechat.INSTANCE, (Consumer<?>) (task) -> runnable.run(), delay <= 0 ? 1 : delay, period);
            }
        }
    }

    @Override
    public void addChannel(Player player, String channel) {
        call(addChannel, player, channel);
    }

    @Override
    public void removeChannel(Player player, String channel) {
        call(removeChannel, player, channel);
    }

    @Override
    public Key createNamespacedKey(String key) {
        return Key.of(key);
    }

    @Override
    public void runTask(Runnable runnable) {
        runTask.accept(runnable);
    }

    @Override
    public void scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
        taskScheduler.scheduleSyncRepeatingTask(runnable, delay, period);
    }

    @Override
    public String getBaseBukkitPackage() {
        return baseBukkitPackage;
    }

    @Override
    public String getBaseServerPackage() {
        return baseServerPackage;
    }

    private interface TaskScheduler {
        void scheduleSyncRepeatingTask(Runnable runnable, long delay, long period);
    }

}
