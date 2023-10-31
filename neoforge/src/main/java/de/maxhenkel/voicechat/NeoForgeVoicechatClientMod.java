package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.integration.clothconfig.ClothConfig;
import de.maxhenkel.voicechat.integration.clothconfig.ClothConfigIntegration;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.NeoForgeClientCompatibilityManager;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeVoicechatClientMod extends VoicechatClient {

    public NeoForgeVoicechatClientMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(((NeoForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE)::onRegisterKeyBinds);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        initializeClient();
        NeoForge.EVENT_BUS.register(ClientCompatibilityManager.INSTANCE);
        ClothConfig.init();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
            if (ClothConfig.isLoaded()) {
                return ClothConfigIntegration.createConfigScreen(parent);
            } else {
                return new VoiceChatSettingsScreen(parent);
            }
        }));
    }

}
