package de.maxhenkel.voicechat;

import com.sun.jna.Platform;
import de.maxhenkel.voicechat.config.ForgeClientConfig;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.integration.clothconfig.ClothConfig;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import de.maxhenkel.voicechat.macos.VersionCheck;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ForgeVoicechatClientMod extends VoicechatClient {

    public ForgeVoicechatClientMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(((ForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE)::onRegisterKeyBinds);
        CLIENT_CONFIG = ForgeVoicechatMod.registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig::new);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        initializeClient();
        MinecraftForge.EVENT_BUS.register(ClientCompatibilityManager.INSTANCE);
        ClothConfig.init();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
            if (ClothConfig.isLoaded()) {
                return ClientCompatibilityManager.INSTANCE.getClothConfigIntegration().createConfigScreen(parent);
            } else {
                return new VoiceChatSettingsScreen(parent);
            }
        }));

        if (Platform.isMac() && !CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            CLIENT_CONFIG.javaMicrophoneImplementation.set(true).save();
        }
        if (Platform.isMac() && CLIENT_CONFIG.useNatives.get() && !VersionCheck.isMacOSNativeCompatible()) {
            CLIENT_CONFIG.useNatives.set(false).save();
        }
    }

}
