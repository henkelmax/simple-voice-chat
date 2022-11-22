package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.command.VoiceChatCommands;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.integration.commodore.CommodoreCommands;
import de.maxhenkel.voicechat.integration.placeholderapi.VoicechatExpansion;
import de.maxhenkel.voicechat.integration.viaversion.ViaVersionCompatibility;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.BukkitVoicechatServiceImpl;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

public final class Voicechat extends JavaPlugin {

    public static Voicechat INSTANCE;

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static int COMPATIBILITY_VERSION = BuildConstants.COMPATIBILITY_VERSION;

    public static ServerConfig SERVER_CONFIG;
    private static YamlConfiguration TRANSLATIONS;
    private static YamlConfiguration DEFAULT_TRANSLATIONS;
    public static ServerVoiceEvents SERVER;

    public static BukkitVoicechatServiceImpl apiService;
    public static NetManager netManager;

    @Override
    public void onEnable() {
        INSTANCE = this;

        if (!BukkitVersionCheck.matchesTargetVersion()) {
            LOGGER.fatal("Disabling Simple Voice Chat due to incompatible Bukkit version!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);

        try {
            LOGGER.info("Loading translations");
            DEFAULT_TRANSLATIONS = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("translations.yml")));
            File file = new File(getDataFolder(), "translations.yml");
            if (file.exists()) {
                TRANSLATIONS = YamlConfiguration.loadConfiguration(file);
                mergeConfigs(TRANSLATIONS, DEFAULT_TRANSLATIONS);
            } else {
                TRANSLATIONS = DEFAULT_TRANSLATIONS;
            }
            TRANSLATIONS.save(file);
        } catch (Exception e) {
            LOGGER.fatal("Failed to load translations! Disabling Simple Voice Chat!", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        netManager = new NetManager();
        netManager.onEnable();

        SERVER_CONFIG = ConfigBuilder.build(getDataFolder().toPath().resolve("voicechat-server.properties"), true, ServerConfig::new);

        apiService = new BukkitVoicechatServiceImpl();
        getServer().getServicesManager().register(BukkitVoicechatService.class, apiService, this, ServicePriority.Normal);

        PluginCommand voicechatCommand = getCommand(VoiceChatCommands.VOICECHAT_COMMAND);
        if (voicechatCommand != null) {
            voicechatCommand.setExecutor(new VoiceChatCommands());

            if (CommodoreProvider.isSupported()) {
                Commodore commodore = CommodoreProvider.getCommodore(this);
                CommodoreCommands.registerCompletions(commodore);
                LOGGER.info("Successfully initialized commodore command completion");
            } else {
                LOGGER.warn("Could not initialize commodore command completion");
            }
        } else {
            LOGGER.error("Failed to register commands");
        }

        try {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new VoicechatExpansion().register();
                LOGGER.info("Successfully registered PlaceholderAPI expansion");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register PlaceholderAPI expansion");
        }

        try {
            if (Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
                ViaVersionCompatibility.register();
                LOGGER.info("Successfully added ViaVersion mappings");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to add ViaVersion mappings");
        }

        if (System.getProperty("VOICECHAT_RELOADED") != null) {
            LOGGER.error("Simple Voice Chat does not support reloads! Expect that things will break!");
        }
        System.setProperty("VOICECHAT_RELOADED", "true");

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            SERVER = new ServerVoiceEvents();
            PluginManager.instance().init();
            SERVER.init();

            Bukkit.getPluginManager().registerEvents(SERVER, this);
            Bukkit.getPluginManager().registerEvents(SERVER.getServer().getPlayerStateManager(), this);
        });
    }

    private static void mergeConfigs(YamlConfiguration base, YamlConfiguration add) {
        Map<String, Object> values = add.getValues(true);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!base.contains(entry.getKey())) {
                base.set(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void onDisable() {
        if (netManager != null) {
            netManager.onDisable();
        }
        getServer().getServicesManager().unregister(apiService);
        if (SERVER != null) {
            SERVER.getServer().close();
        }
    }

    public static String translate(String key) {
        return (String) TRANSLATIONS.get(key);
    }

    public static void logDebug(String message, Object... objects) {
        if (System.getProperty("voicechat.debug") != null) {
            LOGGER.info(message, objects);
        }
    }

}
