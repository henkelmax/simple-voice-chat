package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.BukkitVersion;
import de.maxhenkel.voicechat.Voicechat;

import java.util.HashMap;
import java.util.Map;

public class BukkitCompatibilityManager {

    private static final Map<BukkitVersion, Compatibility> COMPATIBILITIES = new HashMap<>();

    static {
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

    public static Compatibility getCompatibility() throws Exception {
        BukkitVersion version = BukkitVersion.getVersion();
        Voicechat.LOGGER.info("Initializing compatibility for Bukkit version {}", version);
        Compatibility compatibility = COMPATIBILITIES.get(version);
        if (compatibility == null) {
            throw new IncompatibleBukkitVersionException(version, String.format("Incompatible Bukkit version: %s", version));
        }
        return compatibility;
    }

}
