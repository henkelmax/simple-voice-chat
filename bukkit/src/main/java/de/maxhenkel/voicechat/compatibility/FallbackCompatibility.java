package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import de.maxhenkel.voicechat.FallbackTranslations;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class FallbackCompatibility extends BaseCompatibility {

    public static final FallbackCompatibility INSTANCE = new FallbackCompatibility();

    @Override
    public void sendTranslationMessage(Player player, String key, String... args) {
        player.sendMessage(fallbackTranslation(key, args));
    }

    @Override
    public void sendStatusMessage(Player player, String key, String... args) {
        sendTranslationMessage(player, key, args);
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
    public Key createNamespacedKey(String key) {
        BukkitVersion version = BukkitVersion.getVersion();
        if (version == null) {
            return super.createNamespacedKey(key);
        }
        if (version.getMajor() != 1) {
            return super.createNamespacedKey(key);
        }
        if (version.getMinor() > 12) {
            return super.createNamespacedKey(key);
        }
        // Use old channels for versions older than 1.13
        return Key.of(Compatibility1_12.CHANNEL, key);
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

    public static String fallbackTranslation(String key, String... args) {
        String rawTranslation = FallbackTranslations.FALLBACK_TRANSLATIONS.get(key);
        if (rawTranslation == null) {
            return key;
        }

        return String.format(rawTranslation, (Object[]) args);
    }

}
