package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.command.VoicechatCommands;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

public abstract class Voicechat {

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerVoiceEvents SERVER;
    public static ServerConfig SERVER_CONFIG;

    public static int COMPATIBILITY_VERSION = Integer.parseInt("@MOD_COMPATIBILITY_VERSION@");

    public static final Pattern GROUP_REGEX = Pattern.compile("^[^\"\\n\\r\\t\\s][^\"\\n\\r\\t]{0,15}$");

    public void initialize() {
        CommonCompatibilityManager.INSTANCE = createCompatibilityManager();
        PermissionManager.INSTANCE = createPermissionManager();
        LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);
        CommonCompatibilityManager.INSTANCE.getNetManager().init();
        SERVER = new ServerVoiceEvents();
        CommonCompatibilityManager.INSTANCE.onRegisterServerCommands(VoicechatCommands::register);
    }

    protected abstract CommonCompatibilityManager createCompatibilityManager();

    protected abstract PermissionManager createPermissionManager();

    public static void logDebug(String message, Object... objects) {
        if (CommonCompatibilityManager.INSTANCE.isDevEnvironment()) {
            LOGGER.info(message, objects);
        }
    }

}
