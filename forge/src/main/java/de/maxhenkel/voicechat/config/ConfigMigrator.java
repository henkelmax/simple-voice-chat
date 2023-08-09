package de.maxhenkel.voicechat.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.configbuilder.entry.EnumConfigEntry;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigMigrator {

    private static final String MOVED_CONFIG_KEY = "moved";
    private static final LevelResource SERVERCONFIG = new LevelResource("serverconfig");

    public static void migrateClientConfig() {
        migrateConfig(
                ClientConfig.class,
                VoicechatClient.CLIENT_CONFIG,
                Voicechat.getConfigFolder().resolve("voicechat-client.toml"),
                true,
                "config/voicechat/voicechat-client.properties"
        );
    }

    @SubscribeEvent
    public void onLoadLevel(WorldEvent.Load event) {
        if (!(event.getWorld() instanceof ServerLevel serverLevel)) {
            return;
        }
        migrateConfig(
                ServerConfig.class,
                Voicechat.SERVER_CONFIG,
                serverLevel.getServer().getWorldPath(SERVERCONFIG).resolve("voicechat-server.toml"),
                serverLevel.getServer().isDedicatedServer(),
                "config/voicechat/voicechat-server.properties",
                "voice_chat"
        );
    }

    public static <T> void migrateConfig(Class<T> configClass, T modConfig, Path forgeConfig, boolean copyValues, String newPath) {
        migrateConfig(configClass, modConfig, forgeConfig, copyValues, newPath, null);
    }

    public static <T> void migrateConfig(Class<T> configClass, T modConfig, Path forgeConfig, boolean copyValues, String newPath, @Nullable String configKey) {
        try (CommentedFileConfig commentedConfig = CommentedFileConfig.builder(forgeConfig).build()) {
            if (Files.isRegularFile(forgeConfig)) {
                commentedConfig.load();
            }

            Boolean migrated = commentedConfig.get(MOVED_CONFIG_KEY);
            if (migrated != null && migrated) {
                return;
            }

            CommentedConfig config = configKey == null ? commentedConfig : commentedConfig.get(configKey);
            if (config != null && copyValues) {
                migrateConfigValues(configClass, modConfig, config);
            }

            commentedConfig.clear();
            commentedConfig.set(MOVED_CONFIG_KEY, true);
            commentedConfig.setComment(MOVED_CONFIG_KEY, String.format(" This config has been moved to %s", newPath));
            commentedConfig.save();
            Voicechat.LOGGER.info("Successfully migrated config {}", forgeConfig.getFileName());
        }
    }

    public static <T> void migrateConfigValues(Class<T> configClass, T config, CommentedConfig forgeConfig) {
        Field[] declaredFields = configClass.getDeclaredFields();

        ConfigEntry<?> randomEntry = null;
        for (Field field : declaredFields) {
            try {
                field.setAccessible(true);
                Object configEntry = field.get(config);
                if (configEntry instanceof ConfigEntry entry) {
                    if (randomEntry == null) {
                        randomEntry = entry;
                    }
                    Object forgeValue = forgeConfig.get(entry.getKey());
                    copyEntry(forgeValue, entry);
                }
            } catch (IllegalAccessException e) {
                Voicechat.LOGGER.error("Failed to migrate config entry {}", field.getName(), e);
            }
        }
        if (randomEntry != null) {
            randomEntry.save();
        }
    }

    private static void copyEntry(Object forgeValue, ConfigEntry configEntry) {
        try {
            if (forgeValue == null) {
                return;
            }

            if (configEntry instanceof EnumConfigEntry<?>) {
                EnumConfigEntry<?> enumConfigEntry = (EnumConfigEntry<?>) configEntry;
                forgeValue = Enum.valueOf(enumConfigEntry.get().getClass(), forgeValue.toString());
            }

            if (configEntry.getDefault().equals(forgeValue)) {
                return;
            }

            configEntry.set(forgeValue);
            Voicechat.logDebug("Migrated config entry '{}' with value '{}'", configEntry.getKey(), forgeValue);
        } catch (Throwable e) {
            Voicechat.LOGGER.error("Failed to migrate config entry {}", configEntry.getKey(), e);
        }
    }
}
