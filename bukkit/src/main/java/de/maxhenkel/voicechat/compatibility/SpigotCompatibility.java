package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public class SpigotCompatibility extends BaseCompatibility {

    public static final SpigotCompatibility INSTANCE = new SpigotCompatibility();

    private MessageSender messageSender;

    @Override
    public void init() throws Exception {
        super.init();

        Class<?> playerSpigotClass = getClazz("org.bukkit.entity.Player$Spigot");
        Class<?> baseComponentClass = getClazz("net.md_5.bungee.api.chat.BaseComponent");

        Class<?> chatMessageTypeClass = getClazz("net.md_5.bungee.api.ChatMessageType");
        Object systemType = getField(chatMessageTypeClass, "SYSTEM");
        Object actionBarType = getField(chatMessageTypeClass, "ACTION_BAR");

        Method playerSpigot = getMethod(Player.class, "spigot");
        Method sendMessage = getMethod(playerSpigotClass, new String[]{"sendMessage"}, new Class[]{chatMessageTypeClass, baseComponentClass});

        Class<?> translatableComponentClass = getClazz("net.md_5.bungee.api.chat.TranslatableComponent");
        Constructor<?> translatableComponentConstructor = getConstructor(translatableComponentClass, String.class, Object[].class);

        messageSender = ((player, actionBar, key, args) -> {
            Object messageType = actionBar ? actionBarType : systemType;
            call(sendMessage, call(playerSpigot, player), messageType, newInstance(translatableComponentConstructor, key, args));
        });
    }

    public static boolean isSpigotCompatible() {
        return doesMethodExist(Bukkit.class, "spigot");
    }

    @Override
    public Key createNamespacedKey(String key) {
        BukkitVersion version = BukkitVersion.getVersion();
        if (version == null) {
            return super.createNamespacedKey(key);
        }
        if (version.getMajor() != 1) {
            return super.createNamespacedKey(key);
        }
        if (version.getMinor() >= 13) {
            return super.createNamespacedKey(key);
        }
        // Use old channels for versions older than 1.13
        return Key.of(Compatibility1_12.CHANNEL, key);
    }

    @Override
    public void sendTranslationMessage(Player player, String key, String... args) {
        messageSender.send(player, false, key, args);
    }

    @Override
    public void sendStatusMessage(Player player, String key, String... args) {
        messageSender.send(player, true, key, args);
    }

    @Override
    public void sendInviteMessage(Player player, Player commandSender, String groupName, String joinCommand) {
        sendTranslationMessage(player, "message.voicechat.invite", commandSender.getName(), groupName, "");
    }

    @Override
    public void sendIncompatibleMessage(Player player, String pluginVersion, String pluginName) {
        sendTranslationMessage(player, "message.voicechat.incompatible_version", pluginVersion, pluginName);
    }

    @Override
    @Nullable
    public ArgumentType<?> playerArgument() {
        return null;
    }

    @Override
    @Nullable
    public ArgumentType<?> uuidArgument() {
        return null;
    }

    private interface MessageSender {
        void send(Player player, boolean actionBar, String key, String... args);
    }

}
