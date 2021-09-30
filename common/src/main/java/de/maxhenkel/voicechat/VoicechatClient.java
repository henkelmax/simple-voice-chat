package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.ClientConfig;
import de.maxhenkel.voicechat.config.PlayerVolumeConfig;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.resourcepacks.VoiceChatResourcePack;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public abstract class VoicechatClient {

    public static ClientConfig CLIENT_CONFIG;
    public static PlayerVolumeConfig VOLUME_CONFIG;

    public static VoiceChatResourcePack CLASSIC_ICONS;
    public static VoiceChatResourcePack WHITE_ICONS;
    public static VoiceChatResourcePack BLACK_ICONS;

    public void initializeClient() {
        ClientCompatibilityManager.INSTANCE = createCompatibilityManager();

        ConfigBuilder.create(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-client.properties"), builder -> CLIENT_CONFIG = new ClientConfig(builder));

        fixVolumeConfig();
        VOLUME_CONFIG = new PlayerVolumeConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-volumes.properties"));

        //Load instance
        ClientManager.instance();

        CLASSIC_ICONS = new VoiceChatResourcePack("Classic Icons", "classic_icons");
        WHITE_ICONS = new VoiceChatResourcePack("White Icons", "white_icons");
        BLACK_ICONS = new VoiceChatResourcePack("Black Icons", "black_icons");

        ClientCompatibilityManager.INSTANCE.addResourcePackSource(Minecraft.getInstance().getResourcePackRepository(), (Consumer<Pack> consumer, Pack.PackConstructor packConstructor) -> {
            consumer.accept(Pack.create(CLASSIC_ICONS.getName(), false, () -> CLASSIC_ICONS, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN));
            consumer.accept(Pack.create(WHITE_ICONS.getName(), false, () -> WHITE_ICONS, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN));
            consumer.accept(Pack.create(BLACK_ICONS.getName(), false, () -> BLACK_ICONS, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN));
        });
    }

    private void fixVolumeConfig() {
        Path oldLocation = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("voicechat-volumes.properties");
        Path newLocation = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-volumes.properties");
        if (!newLocation.toFile().exists() && oldLocation.toFile().exists()) {
            try {
                Files.move(oldLocation, newLocation, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Voicechat.LOGGER.error("Failed to move volumes config: {}", e.getMessage());
            }
        }
    }

    public abstract ClientCompatibilityManager createCompatibilityManager();

}
