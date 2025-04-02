package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.BukkitVersion;
import de.maxhenkel.voicechat.Voicechat;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BukkitCompatibilityManager {

    private static final Map<BukkitVersion, Compatibility> COMPATIBILITIES = new HashMap<>();

    static {
        COMPATIBILITIES.put(Compatibility1_21_5.VERSION_1_21_5, Compatibility1_21_5.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_3.VERSION_1_21_4, Compatibility1_20_3.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_3.VERSION_1_21_3, Compatibility1_20_3.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_3.VERSION_1_21_2, Compatibility1_20_3.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_3.VERSION_1_21_1, Compatibility1_20_3.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_3.VERSION_1_21, Compatibility1_20_3.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_3.VERSION_1_20_6, Compatibility1_20_3.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_3.VERSION_1_20_5, Compatibility1_20_3.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_3.VERSION_1_20_4, Compatibility1_20_3.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_3.VERSION_1_20_3, Compatibility1_20_3.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20_2.VERSION_1_20_2, Compatibility1_20_2.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20.VERSION_1_20_1, Compatibility1_20.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_20.VERSION_1_20, Compatibility1_20.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_19_1.VERSION_1_19_4, Compatibility1_19_1.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_19_1.VERSION_1_19_3, Compatibility1_19_1.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_19_1.VERSION_1_19_2, Compatibility1_19_1.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_19_1.VERSION_1_19_1, Compatibility1_19_1.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_19.VERSION_1_19, Compatibility1_19.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_18.VERSION_1_18_2, Compatibility1_18.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_18.VERSION_1_18_1, Compatibility1_18.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_18.VERSION_1_18, Compatibility1_18.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_17.VERSION_1_17_1, Compatibility1_17.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_17.VERSION_1_17, Compatibility1_17.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_16.VERSION_1_16_5, Compatibility1_16.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_16.VERSION_1_16_4, Compatibility1_16.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_16.VERSION_1_16_3, Compatibility1_16.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_16.VERSION_1_16_2, Compatibility1_16.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_16.VERSION_1_16_1, Compatibility1_16.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_16.VERSION_1_16, Compatibility1_16.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_12.VERSION_1_12_2, Compatibility1_12.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_12.VERSION_1_12_1, Compatibility1_12.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_12.VERSION_1_12, Compatibility1_12.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_8.VERSION_1_8_8, Compatibility1_8.INSTANCE);
    }

    @Nullable
    public static Compatibility loadCompatibility() {
        BukkitVersion version = BukkitVersion.getVersion();
        if (version == null) {
            return null;
        }
        Voicechat.LOGGER.info("Initializing compatibility for Bukkit version {}", version);
        Compatibility compatibility = COMPATIBILITIES.get(version);
        if (compatibility != null) {
            try {
                compatibility.init();
            } catch (Throwable t) {
                compatibility = null;
                Voicechat.LOGGER.warn("Failed to load compatibility for Bukkit version {}", version, t);
            }
        }
        if (compatibility == null) {
            Voicechat.LOGGER.warn("Incompatible bukkit version {}, trying to fall back to Spigot API compatibility mode", version);
            if (SpigotCompatibility.isSpigotCompatible()) {
                Voicechat.LOGGER.warn("Falling back to compatibility mode, expect issues and lack of features");
                compatibility = SpigotCompatibility.INSTANCE;
                try {
                    compatibility.init();
                } catch (Throwable t) {
                    compatibility = null;
                    Voicechat.LOGGER.warn("Failed to load Spigot API compatibility mode", t);
                }
            } else {
                Voicechat.LOGGER.error("Spigot API not found");
            }
        }

        if (compatibility == null) {
            Voicechat.LOGGER.fatal("Incompatible Bukkit version {}", version);
        }

        return compatibility;
    }

}
