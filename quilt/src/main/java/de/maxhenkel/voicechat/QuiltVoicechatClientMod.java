package de.maxhenkel.voicechat;

import com.sun.jna.Platform;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.QuiltClientConfig;
import de.maxhenkel.voicechat.integration.ClothConfig;
import net.minecraft.client.Minecraft;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class QuiltVoicechatClientMod extends VoicechatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(ModContainer mod) {
        CLIENT_CONFIG = ConfigBuilder.build(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-client.properties"), true, QuiltClientConfig::new);
        initializeClient();
        ClothConfig.init();

        if (Platform.isMac() && !CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            CLIENT_CONFIG.javaMicrophoneImplementation.set(true).save();
        }
    }
}
