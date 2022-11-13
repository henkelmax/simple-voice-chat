package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.ForgeClientConfig;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ForgeVoicechatClientMod extends VoicechatClient {

    public ForgeVoicechatClientMod(FMLPreInitializationEvent event) {
        CLIENT_CONFIG = ConfigBuilder.build(Loader.instance().getConfigDir().toPath().resolve(Voicechat.MODID).resolve("voicechat-client.properties"), true, ForgeClientConfig::new);
    }

    public void clientSetup(FMLInitializationEvent event) {
        initializeClient();
        MinecraftForge.EVENT_BUS.register(ClientCompatibilityManager.INSTANCE);
    }
}
