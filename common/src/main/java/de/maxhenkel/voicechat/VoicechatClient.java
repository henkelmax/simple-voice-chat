package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.config.ClientConfig;
import de.maxhenkel.voicechat.config.PlayerVolumeConfig;
import de.maxhenkel.voicechat.profile.UsernameCache;
import de.maxhenkel.voicechat.resourcepacks.VoiceChatResourcePack;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class VoicechatClient {

    public static ClientConfig CLIENT_CONFIG;
    public static PlayerVolumeConfig VOLUME_CONFIG;
    public static UsernameCache USERNAME_CACHE;

    public static VoiceChatResourcePack CLASSIC_ICONS = new VoiceChatResourcePack("classic_icons", new TextComponentTranslation("resourcepack.voicechat.classic_icons"));
    public static VoiceChatResourcePack WHITE_ICONS = new VoiceChatResourcePack("white_icons", new TextComponentTranslation("resourcepack.voicechat.white_icons"));
    public static VoiceChatResourcePack BLACK_ICONS = new VoiceChatResourcePack("black_icons", new TextComponentTranslation("resourcepack.voicechat.black_icons"));

    public void initializeClient() {
        fixVolumeConfig();
        VOLUME_CONFIG = new PlayerVolumeConfig(Minecraft.getMinecraft().mcDataDir.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-volumes.properties"));
        USERNAME_CACHE = new UsernameCache(Minecraft.getMinecraft().mcDataDir.toPath().resolve("config").resolve(Voicechat.MODID).resolve("username-cache.json").toFile());

        //Load instance
        ClientManager.instance();
    }

    private void fixVolumeConfig() {
        Path oldLocation = Minecraft.getMinecraft().mcDataDir.toPath().resolve("config").resolve("voicechat-volumes.properties");
        Path newLocation = Minecraft.getMinecraft().mcDataDir.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-volumes.properties");
        if (!newLocation.toFile().exists() && oldLocation.toFile().exists()) {
            try {
                Files.move(oldLocation, newLocation, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Voicechat.LOGGER.error("Failed to move volumes config: {}", e.getMessage());
            }
        }
    }
}
