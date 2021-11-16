package de.maxhenkel.voicechat;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Voicechat extends JavaPlugin {

    public static Voicechat INSTANCE;

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static int COMPATIBILITY_VERSION = -1;

    public static ServerConfig SERVER_CONFIG;
    private static FileConfiguration TRANSLATIONS;
    public static ProtocolManager PROTOCOL_MANAGER;

    public static ServerVoiceEvents SERVER;

    public static final Pattern VERSION_REGEX = Pattern.compile("^(\\d+).(\\d+).(\\d+).*$");

    public static BukkitVoicechatServiceImpl apiService;

    @Override
    public void onEnable() {
        INSTANCE = this;

        if (!checkProtocolLib(4, 7, 0)) {
            LOGGER.fatal("This plugin requires ProtocolLib 4.7.0 or later");
            getServer().shutdown();
            return;
        }

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

        SERVER_CONFIG = ConfigBuilder.build(getDataFolder().toPath().resolve("voicechat-server.properties"), true, ServerConfig::new);
        PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();

        apiService = new BukkitVoicechatServiceImpl();
        getServer().getServicesManager().register(BukkitVoicechatService.class, apiService, this, ServicePriority.Normal);

        getCommand("voicechat").setExecutor(new VoiceChatCommands());

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            PluginManager.instance().init(this);

            SERVER = new ServerVoiceEvents(getServer());
            NetManager.onEnable();
            Bukkit.getPluginManager().registerEvents(SERVER, this);
            Bukkit.getPluginManager().registerEvents(SERVER.getServer().getPlayerStateManager(), this);
        });
    }

    @Override
    public void onDisable() {
        if (SERVER != null) {
            NetManager.onDisable();
            SERVER.getServer().close();
        }
    }

    public static String translate(String key) {
        return (String) TRANSLATIONS.get(key);
    }

    private boolean checkProtocolLib(int minMajor, int minMinor, int minPatch) {
        Plugin protocolLib = getServer().getPluginManager().getPlugin("ProtocolLib");
        if (protocolLib == null || !protocolLib.isEnabled()) {
            LOGGER.fatal("ProtocolLib not found");
            return false;
        }
        Matcher m = VERSION_REGEX.matcher(protocolLib.getDescription().getVersion());
        if (!m.matches()) {
            LOGGER.fatal("Failed to parse ProtocolLib version");
            return true;
        }
        int major = Integer.parseInt(m.group(1));
        int minor = Integer.parseInt(m.group(2));
        int patch = Integer.parseInt(m.group(3));

        if (major < minMajor) {
            return false;
        } else if (major == minMajor) {
            if (minor < minMinor) {
                return false;
            } else if (minor == minMinor) {
                return patch >= minPatch;
            }
        }
        return true;
    }
}
