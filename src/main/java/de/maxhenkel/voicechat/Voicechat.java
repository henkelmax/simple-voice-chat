package de.maxhenkel.voicechat;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.command.VoiceChatCommands;
import de.maxhenkel.voicechat.config.ConfigBuilder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public final class Voicechat extends JavaPlugin {

    public static Voicechat INSTANCE;

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static int COMPATIBILITY_VERSION = -1;

    public static MinecraftKey INIT = new MinecraftKey(MODID, "init");

    public static ServerConfig SERVER_CONFIG;
    public static ProtocolManager PROTOCOL_MANAGER;

    public static ServerVoiceEvents SERVER;

    public static final Pattern GROUP_REGEX = Pattern.compile("^\\S[^\"\\n\\r\\t]{0,15}$");

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

        ConfigBuilder.create(getDataFolder().toPath().resolve("voicechat-server.properties"), builder -> SERVER_CONFIG = new ServerConfig(builder));
        PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();

        SERVER = new ServerVoiceEvents(getServer());
        NetManager.onEnable();
        Bukkit.getPluginManager().registerEvents(SERVER, this);
        Bukkit.getPluginManager().registerEvents(SERVER.getServer().getPlayerStateManager(), this);

        this.getCommand("voicechat").setExecutor(new VoiceChatCommands());
    }

    @Override
    public void onDisable() {
        NetManager.onDisable();
        SERVER.getServer().close();
    }
}
