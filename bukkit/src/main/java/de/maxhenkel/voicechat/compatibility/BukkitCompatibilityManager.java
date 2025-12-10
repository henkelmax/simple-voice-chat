package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.BukkitVersion;
import de.maxhenkel.voicechat.Voicechat;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BukkitCompatibilityManager {

    private static final Map<BukkitVersion, Compatibility> COMPATIBILITIES = new HashMap<>();

    static {
        COMPATIBILITIES.put(Compatibility1_21_5.VERSION_1_21_11, Compatibility1_21_5.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_21_5.VERSION_1_21_10, Compatibility1_21_5.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_21_5.VERSION_1_21_9, Compatibility1_21_5.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_21_5.VERSION_1_21_8, Compatibility1_21_5.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_21_5.VERSION_1_21_7, Compatibility1_21_5.INSTANCE);
        COMPATIBILITIES.put(Compatibility1_21_5.VERSION_1_21_6, Compatibility1_21_5.INSTANCE);
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
        BukkitVersion version;

        BukkitVersion forcedVersion = getForcedVersion();
        if (forcedVersion != null) {
            version = forcedVersion;
        } else {
            version = BukkitVersion.getVersion();
        }

        if (version == null) {
            return null;
        }

        Voicechat.LOGGER.info("Initializing compatibility for version {}", version);

        if (version.getMajor() == 1 && version.getMinor() <= 7) {
            Voicechat.LOGGER.fatal("You are running a server that's too old to be able to join with any voice chat client");
            return null;
        }

        Compatibility compatibility = COMPATIBILITIES.get(version);
        if (compatibility != null) {
            try {
                compatibility.init();
            } catch (Throwable t) {
                compatibility = null;
                Voicechat.LOGGER.warn("Failed to load compatibility for version {}", version, t);
            }
        }
        if (compatibility == null) {
            Voicechat.LOGGER.warn("Incompatible version {}, trying to fall back to Spigot API compatibility mode", version);
            if (SpigotCompatibility.isSpigotCompatible()) {
                compatibility = SpigotCompatibility.INSTANCE;
                try {
                    compatibility.init();
                    Voicechat.LOGGER.warn("Falling back to Spigot API compatibility mode, expect issues and lack of features");
                } catch (CompatibilityReflectionException e) {
                    compatibility = null;
                    // Only log the message in case reflection fails
                    Voicechat.LOGGER.warn("Failed to load Spigot API compatibility mode: {}", e.getMessage());
                } catch (Throwable t) {
                    compatibility = null;
                    Voicechat.LOGGER.warn("Failed to load Spigot API compatibility mode", t);
                }
            } else {
                Voicechat.LOGGER.warn("Spigot API not found");
            }
        }

        if (compatibility == null) {
            compatibility = FallbackCompatibility.INSTANCE;
            try {
                compatibility.init();
                Voicechat.LOGGER.warn("Falling back to Bukkit compatibility mode, expect issues and lack of features");
                Voicechat.LOGGER.warn("Chat messages won't be translatable");
            } catch (CompatibilityReflectionException e) {
                compatibility = null;
                // Only log the message in case reflection fails
                Voicechat.LOGGER.warn("Failed to load Bukkit compatibility mode: {}", e.getMessage());
            } catch (Throwable t) {
                compatibility = null;
                Voicechat.LOGGER.warn("Failed to load Bukkit compatibility mode", t);
            }
        }

        if (compatibility == null) {
            Voicechat.LOGGER.fatal("Could not load any compatibility for {}", version);
        }

        return compatibility;
    }

    @Nullable
    public static BukkitVersion getForcedVersion() {
        String property = System.getProperty("voicechat.compatibility");
        if (property == null) {
            return null;
        }
        BukkitVersion forcedVersion = BukkitVersion.parseBukkitVersion(property);
        if (forcedVersion == null) {
            Voicechat.LOGGER.warn("Failed to parse forced compatibility version: {}", property);
            return null;
        }
        Voicechat.LOGGER.info("Forcing compatibility for {}", forcedVersion);
        Voicechat.LOGGER.warn("Forcing version compatibility could lead to issues - use with caution!");
        return forcedVersion;
    }

}
