package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public abstract class BaseCompatibility implements Compatibility {

    private String baseBukkitPackage;
    private String baseServerPackage;

    private Method addChannel;
    private Method removeChannel;
    @Nullable
    private Class<? extends PlayerEvent> playerHideEntityEvent;
    @Nullable
    private Method playerHideEntityEventGetEntity;
    @Nullable
    private Class<? extends PlayerEvent> playerShowEntityEvent;
    @Nullable
    private Method playerShowEntityEventGetEntity;
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

        try {
            if (doesClassExist("org.bukkit.event.player.PlayerHideEntityEvent") && doesClassExist("org.bukkit.event.player.PlayerShowEntityEvent")) {
                Class<?> hide = ReflectionUtils.getClazz("org.bukkit.event.player.PlayerHideEntityEvent");
                if (!PlayerEvent.class.isAssignableFrom(hide)) {
                    throw new CompatibilityReflectionException("PlayerHideEntityEvent is not a subclass of PlayerEvent");
                }

                Class<?> show = ReflectionUtils.getClazz("org.bukkit.event.player.PlayerShowEntityEvent");
                if (!PlayerEvent.class.isAssignableFrom(show)) {
                    throw new CompatibilityReflectionException("PlayerShowEntityEvent is not a subclass of PlayerEvent");
                }
                playerShowEntityEventGetEntity = getMethod(show, "getEntity");
                playerHideEntityEventGetEntity = getMethod(hide, "getEntity");
                playerShowEntityEvent = (Class<? extends PlayerEvent>) show;
                playerHideEntityEvent = (Class<? extends PlayerEvent>) hide;
            } else {
                Voicechat.LOGGER.warn("Vanish plugin integration is not supported");
            }
        } catch (CompatibilityReflectionException e) {
            Voicechat.LOGGER.warn("Vanish plugin integration is disabled");
            Voicechat.LOGGER.warn("Failed to load vanish compatibility", e);
            playerHideEntityEvent = null;
            playerShowEntityEvent = null;
        }

        runTask = runnable -> Bukkit.getScheduler().runTask(Voicechat.INSTANCE, runnable);
        taskScheduler = new TaskScheduler() {
            @Override
            public void scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(Voicechat.INSTANCE, runnable, delay, period);
            }

            @Override
            public void runTaskLater(Runnable runnable, long delay) {
                Bukkit.getScheduler().runTaskLater(Voicechat.INSTANCE, runnable, delay);
            }
        };
        if (doesMethodExist(Bukkit.class, "getGlobalRegionScheduler")) {
            Object globalRegionScheduler = callMethod(Bukkit.class, "getGlobalRegionScheduler");

            Method run = getMethod(globalRegionScheduler.getClass(), new String[]{"run"}, new Class[]{Plugin.class, Consumer.class});
            runTask = runnable -> call(run, globalRegionScheduler, Voicechat.INSTANCE, (Consumer<?>) (task) -> runnable.run());

            if (
                    doesMethodExist(globalRegionScheduler.getClass(), "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class)
                            && doesMethodExist(globalRegionScheduler.getClass(), "runDelayed", Plugin.class, Consumer.class, long.class)
            ) {
                Method runAtFixedRate = getMethod(globalRegionScheduler.getClass(), new String[]{"runAtFixedRate"}, new Class[]{Plugin.class, Consumer.class, long.class, long.class});
                Method runDelayed = getMethod(globalRegionScheduler.getClass(), new String[]{"runDelayed"}, new Class[]{Plugin.class, Consumer.class, long.class});
                taskScheduler = new TaskScheduler() {
                    @Override
                    public void scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
                        call(runAtFixedRate, globalRegionScheduler, Voicechat.INSTANCE, (Consumer<?>) (task) -> runnable.run(), delay <= 0 ? 1 : delay, period);
                    }

                    @Override
                    public void runTaskLater(Runnable runnable, long delay) {
                        call(runDelayed, globalRegionScheduler, Voicechat.INSTANCE, (Consumer<?>) (task) -> runnable.run(), delay <= 0 ? 1 : delay);
                    }
                };
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
    public void runTaskLater(Runnable runnable, long delay) {
        taskScheduler.runTaskLater(runnable, delay);
    }

    @Override
    public boolean canSee(Player player, Player other) {
        if (playerHideEntityEvent == null || playerShowEntityEvent == null) {
            // Don't use canSee if hide and show events are missing
            // This avoids state desyncs on older versions that have canSee but not the events
            return true;
        }
        return player.canSee(other);
    }

    @Override
    public void registerPlayerHideEvent(Consumer<PlayerHideEvent> event) {
        if (playerHideEntityEvent == null) {
            return;
        }
        Bukkit.getPluginManager().registerEvent(
                playerHideEntityEvent,
                new Listener() {
                },
                EventPriority.NORMAL,
                (listener, evt) -> {
                    if (!evt.getClass().isAssignableFrom(playerHideEntityEvent)) {
                        return;
                    }
                    PlayerEvent playerEvent = (PlayerEvent) evt;
                    Object entityObj = call(playerHideEntityEventGetEntity, playerEvent);
                    if (!(entityObj instanceof Player)) {
                        return;
                    }
                    event.accept(new PlayerHideEvent((Player) entityObj, playerEvent.getPlayer()));
                },
                Voicechat.INSTANCE
        );
    }

    @Override
    public void registerPlayerShowEvent(Consumer<PlayerShowEvent> event) {
        if (playerShowEntityEvent == null) {
            return;
        }
        Bukkit.getPluginManager().registerEvent(
                playerShowEntityEvent,
                new Listener() {
                },
                EventPriority.NORMAL,
                (listener, evt) -> {
                    if (!evt.getClass().isAssignableFrom(playerShowEntityEvent)) {
                        return;
                    }
                    PlayerEvent playerEvent = (PlayerEvent) evt;
                    Object entityObj = call(playerShowEntityEventGetEntity, playerEvent);
                    if (!(entityObj instanceof Player)) {
                        return;
                    }
                    event.accept(new PlayerShowEvent((Player) entityObj, playerEvent.getPlayer()));
                },
                Voicechat.INSTANCE
        );
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

        void runTaskLater(Runnable runnable, long delay);
    }

}
