package de.maxhenkel.voicechat.util;

import de.maxhenkel.voicechat.net.NetManager;
import org.bukkit.NamespacedKey;

public class NamespacedKeyUtil {

    public static NamespacedKey voicechat(String key) {
        return new NamespacedKey(NetManager.CHANNEL, key);
    }

}
