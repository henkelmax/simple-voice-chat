package de.maxhenkel.voicechat;

import com.sun.jna.Platform;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.CategoryVolumeConfig;
import de.maxhenkel.voicechat.config.ClientConfig;
import de.maxhenkel.voicechat.config.PlayerVolumeConfig;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.macos.VersionCheck;
import de.maxhenkel.voicechat.natives.LameManager;
import de.maxhenkel.voicechat.natives.OpusManager;
import de.maxhenkel.voicechat.natives.RNNoiseManager;
import de.maxhenkel.voicechat.natives.SpeexManager;
import de.maxhenkel.voicechat.profile.UsernameCache;
import de.maxhenkel.voicechat.resourcepacks.VoiceChatResourcePack;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import net.minecraft.network.chat.Component;

public abstract class VoicechatClient {

    public static ClientConfig CLIENT_CONFIG;
    public static PlayerVolumeConfig PLAYER_VOLUME_CONFIG;
    public static CategoryVolumeConfig CATEGORY_VOLUME_CONFIG;
    public static UsernameCache USERNAME_CACHE;

    public static VoiceChatResourcePack CLASSIC_ICONS;
    public static VoiceChatResourcePack WHITE_ICONS;
    public static VoiceChatResourcePack BLACK_ICONS;

    public VoicechatClient() {
        KeyEvents.registerKeyBinds();

        CLASSIC_ICONS = new VoiceChatResourcePack("classic_icons", Component.translatable("resourcepack.voicechat.classic_icons"));
        WHITE_ICONS = new VoiceChatResourcePack("white_icons", Component.translatable("resourcepack.voicechat.white_icons"));
        BLACK_ICONS = new VoiceChatResourcePack("black_icons", Component.translatable("resourcepack.voicechat.black_icons"));

        ClientCompatibilityManager.INSTANCE.addResourcePackSource(consumer -> {
            consumer.accept(CLASSIC_ICONS.toPack());
            consumer.accept(WHITE_ICONS.toPack());
            consumer.accept(BLACK_ICONS.toPack());
        });
    }

    public void initializeConfigs() {
        CLIENT_CONFIG = ConfigBuilder.builder(ClientConfig::new).path(Voicechat.getVoicechatConfigFolder().resolve("voicechat-client.properties")).migration(ClientConfig::migrate).build();
        PLAYER_VOLUME_CONFIG = new PlayerVolumeConfig(Voicechat.getVoicechatConfigFolder().resolve("player-volumes.properties"));
        CATEGORY_VOLUME_CONFIG = new CategoryVolumeConfig(Voicechat.getVoicechatConfigFolder().resolve("category-volumes.properties"));
        USERNAME_CACHE = new UsernameCache(Voicechat.getVoicechatConfigFolder().resolve("username-cache.json").toFile());
    }

    public void initializeClient() {
        initializeConfigs();

        //Load instance
        ClientManager.instance();

        OpusManager.init();
        RNNoiseManager.init();
        SpeexManager.init();
        LameManager.init();

        if (Platform.isMac()) {
            if (!VersionCheck.isMacOSNativeCompatible()) {
                Voicechat.LOGGER.warn("Your MacOS version is incompatible with {}", CommonCompatibilityManager.INSTANCE.getModName());
            }
            if (!CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
                CLIENT_CONFIG.javaMicrophoneImplementation.set(true).save();
            }
        }
    }
}
