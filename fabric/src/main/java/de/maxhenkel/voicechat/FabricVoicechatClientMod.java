package de.maxhenkel.voicechat;

import com.sun.jna.Platform;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.FabricClientConfig;
import de.maxhenkel.voicechat.integration.ClothConfig;
import de.maxhenkel.voicechat.macos.VersionCheck;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class FabricVoicechatClientMod extends VoicechatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CLIENT_CONFIG = ConfigBuilder.build(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-client.properties"), true, FabricClientConfig::new);
        initializeClient();
        ClothConfig.init();

        if (Platform.isMac() && !CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            CLIENT_CONFIG.javaMicrophoneImplementation.set(true).save();
        }
        if (Platform.isMac() && CLIENT_CONFIG.useNatives.get() && !VersionCheck.isMacOSNativeCompatible()) {
            CLIENT_CONFIG.useNatives.set(false).save();
        }
    }

}
