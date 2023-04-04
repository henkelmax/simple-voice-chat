package de.maxhenkel.voicechat;

import com.sun.jna.Platform;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.ClientConfig;
import de.maxhenkel.voicechat.config.VolumeConfig;
import de.maxhenkel.voicechat.integration.clothconfig.ClothConfig;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.macos.VersionCheck;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusManager;
import de.maxhenkel.voicechat.profile.UsernameCache;
import de.maxhenkel.voicechat.resourcepacks.VoiceChatResourcePack;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public abstract class VoicechatClient {

    public static ClientConfig CLIENT_CONFIG;
    public static VolumeConfig VOLUME_CONFIG;
    public static UsernameCache USERNAME_CACHE;

    public static VoiceChatResourcePack CLASSIC_ICONS;
    public static VoiceChatResourcePack WHITE_ICONS;
    public static VoiceChatResourcePack BLACK_ICONS;

    public VoicechatClient() {
        KeyEvents.registerKeyBinds();

        CLASSIC_ICONS = new VoiceChatResourcePack("classic_icons", Component.translatable("resourcepack.voicechat.classic_icons"));
        WHITE_ICONS = new VoiceChatResourcePack("white_icons", Component.translatable("resourcepack.voicechat.white_icons"));
        BLACK_ICONS = new VoiceChatResourcePack("black_icons", Component.translatable("resourcepack.voicechat.black_icons"));

        ClientCompatibilityManager.INSTANCE.addResourcePackSource(Minecraft.getInstance().getResourcePackRepository(), (Consumer<Pack> consumer, Pack.PackConstructor packConstructor) -> {
            consumer.accept(CLASSIC_ICONS.toPack());
            consumer.accept(WHITE_ICONS.toPack());
            consumer.accept(BLACK_ICONS.toPack());
        });
    }

    public void initializeConfigs() {
        fixVolumeConfig();
        CLIENT_CONFIG = ConfigBuilder.build(Voicechat.getModConfigFolder().resolve("voicechat-client.properties"), true, ClientConfig::new);
        VOLUME_CONFIG = new VolumeConfig(Voicechat.getModConfigFolder().resolve("voicechat-volumes.properties"));
        USERNAME_CACHE = new UsernameCache(Voicechat.getModConfigFolder().resolve("username-cache.json").toFile());
    }

    public void initializeClient() {
        initializeConfigs();

        //Load instance
        ClientManager.instance();

        ClothConfig.init();

        OpusManager.opusNativeCheck();

        if (Platform.isMac()) {
            if (!VersionCheck.isMacOSNativeCompatible()) {
                Voicechat.LOGGER.warn("Your MacOS version is incompatible with {}", CommonCompatibilityManager.INSTANCE.getModName());
            }
            if (!CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
                CLIENT_CONFIG.javaMicrophoneImplementation.set(true).save();
            }
        }
    }

    private void fixVolumeConfig() {
        Path oldLocation = Voicechat.getConfigFolder().resolve("voicechat-volumes.properties");
        Path newLocation = Voicechat.getModConfigFolder().resolve("voicechat-volumes.properties");
        if (!newLocation.toFile().exists() && oldLocation.toFile().exists()) {
            try {
                Files.move(oldLocation, newLocation, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Voicechat.LOGGER.error("Failed to move volumes config", e);
            }
        }
    }
}
