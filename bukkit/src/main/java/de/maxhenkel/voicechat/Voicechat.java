package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.command.VoiceChatCommands;
import de.maxhenkel.voicechat.compatibility.BukkitCompatibilityManager;
import de.maxhenkel.voicechat.compatibility.Compatibility;
import de.maxhenkel.voicechat.compatibility.IncompatibleBukkitVersionException;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.config.Translations;
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
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Pattern;

public final class Voicechat extends JavaPlugin {

    public static Voicechat INSTANCE;

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static int COMPATIBILITY_VERSION = BuildConstants.COMPATIBILITY_VERSION;

    public static ServerConfig SERVER_CONFIG;
    public static Translations TRANSLATIONS;
    public static ServerVoiceEvents SERVER;

    public static BukkitVoicechatServiceImpl apiService;
    public static NetManager netManager;
    public static Compatibility compatibility;

    public static final Pattern GROUP_REGEX = Pattern.compile("^[^\\n\\r\\t\\s][^\\n\\r\\t]{0,23}$");

    @Override
    public void onEnable() {
        INSTANCE = this;

        try {
            compatibility = BukkitCompatibilityManager.getCompatibility();
        } catch (IncompatibleBukkitVersionException e) {
            LOGGER.fatal("Incompatible Bukkit version: {}", e.getVersion());
            LOGGER.fatal("Disabling Simple Voice Chat");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } catch (Throwable t) {
            LOGGER.fatal("Failed to load compatibility", t);
            LOGGER.fatal("Disabling Simple Voice Chat");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);

        SERVER_CONFIG = ConfigBuilder.build(getDataFolder().toPath().resolve("voicechat-server.properties"), true, ServerConfig::new);
        TRANSLATIONS = ConfigBuilder.build(getDataFolder().toPath().resolve("translations.properties"), true, Translations::new);

        netManager = new NetManager();
        netManager.onEnable();

        apiService = new BukkitVoicechatServiceImpl();
        getServer().getServicesManager().register(BukkitVoicechatService.class, apiService, this, ServicePriority.Normal);

        PluginCommand voicechatCommand = getCommand(VoiceChatCommands.VOICECHAT_COMMAND);
        if (voicechatCommand != null) {
            VoiceChatCommands voiceChatCommands = new VoiceChatCommands();
            voicechatCommand.setExecutor(voiceChatCommands);
            voicechatCommand.setTabCompleter(voiceChatCommands);
            try {
                if (CommodoreProvider.isSupported()) {
                    Commodore commodore = CommodoreProvider.getCommodore(this);
                    CommodoreCommands.registerCompletions(commodore);
                    LOGGER.info("Successfully initialized commodore command completion");
                } else {
                    LOGGER.warn("Commodore command completion is not supported");
                }
            } catch (Throwable t) {
                LOGGER.warn("Failed to initialize commodore command completion", t);
            }
        } else {
            LOGGER.error("Failed to register commands");
        }

        try {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new VoicechatExpansion().register();
                LOGGER.info("Successfully registered PlaceholderAPI expansion");
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to register PlaceholderAPI expansion", t);
        }

        try {
            if (Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
                ViaVersionCompatibility.register();
                LOGGER.info("Successfully added ViaVersion mappings");
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to add ViaVersion mappings", t);
        }

        if (System.getProperty("VOICECHAT_RELOADED") != null) {
            LOGGER.error("Simple Voice Chat does not support reloads! Expect that things will break!");
        }
        System.setProperty("VOICECHAT_RELOADED", "true");

        compatibility.runTask(() -> {
            SERVER = new ServerVoiceEvents();
            PluginManager.instance().init();
            SERVER.init();

            Bukkit.getPluginManager().registerEvents(SERVER, this);
            Bukkit.getPluginManager().registerEvents(SERVER.getServer().getPlayerStateManager(), this);
        });
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

    public static void logDebug(String message, Object... objects) {
        if (System.getProperty("voicechat.debug") != null) {
            LOGGER.info(message, objects);
        }
    }

}
