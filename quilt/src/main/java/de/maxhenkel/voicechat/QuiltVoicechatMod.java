package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.QuiltServerConfig;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class QuiltVoicechatMod extends Voicechat implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {
        SERVER_CONFIG = ConfigBuilder.build(QuiltLoader.getConfigDir().resolve(MODID).resolve("voicechat-server.properties"), true, QuiltServerConfig::new);

        initialize();
    }

}
