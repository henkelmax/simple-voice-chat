package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

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
}
