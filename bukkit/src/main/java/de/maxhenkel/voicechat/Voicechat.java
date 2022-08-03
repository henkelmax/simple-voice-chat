package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.command.VoiceChatCommands;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.integration.commodore.CommodoreCommands;
import de.maxhenkel.voicechat.integration.placeholderapi.VoicechatExpansion;
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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.Manifest;

public final class Voicechat extends JavaPlugin {

    public static Voicechat INSTANCE;

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static int COMPATIBILITY_VERSION;

    static {
        try {
            COMPATIBILITY_VERSION = readVersion();
        } catch (Exception e) {
            LOGGER.fatal("Failed to read compatibility version: {}", e.getMessage());
            COMPATIBILITY_VERSION = -1;
        }
    }

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

        if (System.getProperty("VOICECHAT_RELOADED") != null) {
            LOGGER.error("Simple Voice Chat does not support reloads! Expect that things will break!");
        }
        System.setProperty("VOICECHAT_RELOADED", "true");

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            PluginManager.instance().init(this);

            SERVER = new ServerVoiceEvents(getServer());
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

    private static int readVersion() throws IOException {
        String ver = readMetaInf("Compatibility-Version");
        if (ver != null) {
            return Integer.parseInt(ver);
        }
        throw new IOException("Could not read MANIFEST.MF");
    }

    @Nullable
    public static String readMetaInf(String key) throws IOException {
        Enumeration<URL> resources = Voicechat.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            String value = manifest.getMainAttributes().getValue(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

}
