package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.config.ClientConfig;
import de.maxhenkel.voicechat.config.PlayerVolumeConfig;
import de.maxhenkel.voicechat.profile.UsernameCache;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class VoicechatClient {

    public static ClientConfig CLIENT_CONFIG;
    public static PlayerVolumeConfig VOLUME_CONFIG;
    public static UsernameCache USERNAME_CACHE;

    // public static VoiceChatResourcePack CLASSIC_ICONS;
    // public static VoiceChatResourcePack WHITE_ICONS;
    // public static VoiceChatResourcePack BLACK_ICONS;

    public void initializeClient() {
        fixVolumeConfig();
        VOLUME_CONFIG = new PlayerVolumeConfig(Minecraft.getMinecraft().mcDataDir.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-volumes.properties"));
        USERNAME_CACHE = new UsernameCache(Minecraft.getMinecraft().mcDataDir.toPath().resolve("config").resolve(Voicechat.MODID).resolve("username-cache.json").toFile());

        //Load instance
        ClientManager.instance();

        //TODO Fix resource packs
        /*CLASSIC_ICONS = new VoiceChatResourcePack("classic_icons", new TranslationTextComponent("resourcepack.voicechat.classic_icons"));
        WHITE_ICONS = new VoiceChatResourcePack("white_icons", new TranslationTextComponent("resourcepack.voicechat.white_icons"));
        BLACK_ICONS = new VoiceChatResourcePack("black_icons", new TranslationTextComponent("resourcepack.voicechat.black_icons"));

        ClientCompatibilityManager.INSTANCE.addResourcePackSource(Minecraft.getInstance().getResourcePackRepository(), (Consumer<ResourcePackInfo> consumer, ResourcePackInfo.IFactory packConstructor) -> {
            consumer.accept(ResourcePackInfo.create(CLASSIC_ICONS.getName(), false, () -> CLASSIC_ICONS, packConstructor, ResourcePackInfo.Priority.TOP, IPackNameDecorator.BUILT_IN));
            consumer.accept(ResourcePackInfo.create(WHITE_ICONS.getName(), false, () -> WHITE_ICONS, packConstructor, ResourcePackInfo.Priority.TOP, IPackNameDecorator.BUILT_IN));
            consumer.accept(ResourcePackInfo.create(BLACK_ICONS.getName(), false, () -> BLACK_ICONS, packConstructor, ResourcePackInfo.Priority.TOP, IPackNameDecorator.BUILT_IN));
        });*/
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
