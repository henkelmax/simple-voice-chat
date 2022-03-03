package de.maxhenkel.voicechat;

import com.sun.jna.Platform;
import de.maxhenkel.voicechat.config.ForgeClientConfig;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ForgeVoicechatClientMod extends VoicechatClient {

    private final ClientCompatibilityManager compatibilityManager;

    public ForgeVoicechatClientMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        compatibilityManager = new ForgeClientCompatibilityManager();
        CLIENT_CONFIG = ForgeVoicechatMod.registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig::new);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        initializeClient();
        MinecraftForge.EVENT_BUS.register(compatibilityManager);
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((client, parent) -> {
            return new VoiceChatSettingsScreen(parent);
        }));

        if (Platform.isMac() && !CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            CLIENT_CONFIG.javaMicrophoneImplementation.set(true).save();
        }
    }

    @Override
    public ClientCompatibilityManager createCompatibilityManager() {
        return compatibilityManager;
    }
}
