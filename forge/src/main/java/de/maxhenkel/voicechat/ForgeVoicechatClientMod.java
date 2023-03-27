package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.config.ConfigMigrator;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.integration.clothconfig.ClothConfig;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler;
import de.maxhenkel.voicechat.macos.VersionCheck;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ForgeVoicechatClientMod extends VoicechatClient {

    public ForgeVoicechatClientMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        initializeClient();
        MinecraftForge.EVENT_BUS.register(ClientCompatibilityManager.INSTANCE);
        ClothConfig.init();
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((client, parent) -> {
            if (ClothConfig.isLoaded()) {
                return ClientCompatibilityManager.INSTANCE.getClothConfigIntegration().createConfigScreen(parent);
            } else {
                return new VoiceChatSettingsScreen(parent);
            }
        }));
    }

    @Override
    public void initializeConfigs() {
        super.initializeConfigs();
        ConfigMigrator.migrateClientConfig();
    }

}
