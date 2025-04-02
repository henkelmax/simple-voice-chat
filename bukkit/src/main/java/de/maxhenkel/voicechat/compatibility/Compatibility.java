package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public interface Compatibility {

    default void init() throws Exception {

    }

    void addChannel(Player player, String channel);

    void removeChannel(Player player, String channel);

    Key createNamespacedKey(String key);

    void sendTranslationMessage(Player player, String key, String... args);

    void sendStatusMessage(Player player, String key, String... args);

    void sendInviteMessage(Player player, Player commandSender, String groupName, String joinCommand);

    void sendIncompatibleMessage(Player player, String pluginVersion, String pluginName);

    void runTask(Runnable runnable);

    void scheduleSyncRepeatingTask(Runnable runnable, long delay, long period);

    @Nullable
    ArgumentType<?> playerArgument();

    @Nullable
    ArgumentType<?> uuidArgument();

    String getBaseBukkitPackage();

    String getBaseServerPackage();

    /**
     * @param classNames the class names including the package name, starting after the bukkit the base package e.g. <code>org.bukkit.craftbukkit.v1_20_R3.CraftServer</code> would be <code>CraftServer</code>
     * @return the class
     */
    default Class<?> getBukkitClass(String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(String.format("%s.%s", getBaseBukkitPackage(), className));
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException(String.format("Could not find any of the following classes: %s", String.join(", ", classNames)));
    }

    default Class<?> getServerClass(String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(String.format("%s.%s", getBaseServerPackage(), className));
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException(String.format("Could not find any of the following classes: %s", String.join(", ", classNames)));
    }

}
