package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

public abstract class Voicechat {

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerVoiceEvents SERVER;
    public static ServerConfig SERVER_CONFIG;

    public static int COMPATIBILITY_VERSION = -1;

    public static final Pattern GROUP_REGEX = Pattern.compile("^\\S[^\"\\n\\r\\t]{0,15}$");

    public void initialize() {
        CommonCompatibilityManager.INSTANCE = createCompatibilityManager();
        try {
            COMPATIBILITY_VERSION = readCompatibilityVersion();
            LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);
        } catch (Exception e) {
            LOGGER.error("Failed to read compatibility version");
        }
        CommonCompatibilityManager.INSTANCE.onServerStarting(server -> {
            ConfigBuilder.create(server.getServerDirectory().toPath().resolve("config").resolve(MODID).resolve("voicechat-server.properties"), builder -> SERVER_CONFIG = new ServerConfig(builder));
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().init();
        SERVER = new ServerVoiceEvents();
    }

    public abstract int readCompatibilityVersion() throws Exception;

    public abstract CommonCompatibilityManager createCompatibilityManager();

}
