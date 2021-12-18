package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.command.VoiceChatCommands;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.BukkitVoicechatServiceImpl;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public final class Voicechat extends JavaPlugin {

    public static Voicechat INSTANCE;

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static int COMPATIBILITY_VERSION = -1;

    public static ServerConfig SERVER_CONFIG;
    private static FileConfiguration TRANSLATIONS;
    public static ServerVoiceEvents SERVER;

    public static BukkitVoicechatServiceImpl apiService;
    public static NetManager netManager;

    @Override
    public void onEnable() {
        INSTANCE = this;

        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("compatibility.properties");
            Properties props = new Properties();
            props.load(in);
            COMPATIBILITY_VERSION = Integer.parseInt(props.getProperty("compatibility_version"));
            LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);
        } catch (Exception e) {
            LOGGER.error("Failed to read compatibility version");
        }

        try {
            LOGGER.info("Loading translations");
            File file = new File(getDataFolder(), "translations.yml");
            if (!file.exists()) {
                TRANSLATIONS = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("translations.yml")));
                saveResource("translations.yml", false);
            }
            TRANSLATIONS = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            LOGGER.fatal("Failed to load translations");
            e.printStackTrace();
            getServer().shutdown();
            return;
        }

        netManager = new NetManager();
        netManager.onEnable();

        SERVER_CONFIG = ConfigBuilder.build(getDataFolder().toPath().resolve("voicechat-server.properties"), true, ServerConfig::new);

        apiService = new BukkitVoicechatServiceImpl();
        getServer().getServicesManager().register(BukkitVoicechatService.class, apiService, this, ServicePriority.Normal);

        getCommand("voicechat").setExecutor(new VoiceChatCommands());

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            PluginManager.instance().init(this);

            SERVER = new ServerVoiceEvents(getServer());
            Bukkit.getPluginManager().registerEvents(SERVER, this);
            Bukkit.getPluginManager().registerEvents(SERVER.getServer().getPlayerStateManager(), this);
        });
    }

    @Override
    public void onDisable() {
        netManager.onDisable();
        getServer().getServicesManager().unregister(apiService);
        if (SERVER != null) {
            SERVER.getServer().close();
        }
    }

    public static String translate(String key) {
        return (String) TRANSLATIONS.get(key);
    }
}
