package de.maxhenkel.voicechat.compatibility;

import org.bukkit.entity.Player;

public abstract class JsonMessageBaseCompatibility extends BaseCompatibility {

    @Override
    public void sendTranslationMessage(Player player, String key, String... args) {
        sendJsonMessage(player, createTranslationMessage(key, args));
    }

    @Override
    public void sendStatusMessage(Player player, String key, String... args) {
        sendJsonStatusMessage(player, createTranslationMessage(key, args));
    }

    public abstract void sendJsonMessage(Player player, String json);

    public abstract void sendJsonStatusMessage(Player player, String json);

    public abstract String createTranslationMessage(String key, String... args);

}
