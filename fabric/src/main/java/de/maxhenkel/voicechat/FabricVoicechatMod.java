package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.FabricServerConfig;
import de.maxhenkel.voicechat.integration.ViaVersionCompatibility;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class FabricVoicechatMod extends Voicechat implements ModInitializer {

    @Override
    public void onInitialize() {
        SERVER_CONFIG = ConfigBuilder.build(FabricLoader.getInstance().getConfigDir().resolve(MODID).resolve("voicechat-server.properties"), true, FabricServerConfig::new);

        initialize();

        ViaVersionCompatibility.register();
    }

}
