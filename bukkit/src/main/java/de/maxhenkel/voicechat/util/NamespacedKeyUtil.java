package de.maxhenkel.voicechat.util;

import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.NamespacedKey;

public class NamespacedKeyUtil {

    public static NamespacedKey voicechat(String key) {
        return new NamespacedKey(Voicechat.MODID, key);
    }

}
