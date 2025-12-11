package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.config.ConfigMigrator;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ForgeVoicechatClientMod extends VoicechatClient {

    protected FMLJavaModLoadingContext context;

    public ForgeVoicechatClientMod(FMLJavaModLoadingContext context) {
        this.context = context;
        ForgeClientCompatibilityManager clientCompatibilityManager = (ForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE;
        RenderNameTagEvent.BUS.addListener(clientCompatibilityManager::onRenderName);
        AddGuiOverlayLayersEvent.BUS.addListener(clientCompatibilityManager::onAddGuiOverlayLayers);
        InputEvent.Key.BUS.addListener(clientCompatibilityManager::onKey);
        InputEvent.MouseButton.Pre.BUS.addListener(clientCompatibilityManager::onMouse);
        TickEvent.ClientTickEvent.Pre.BUS.addListener(clientCompatibilityManager::onClientTick);
        TickEvent.ClientTickEvent.Post.BUS.addListener(clientCompatibilityManager::onKeyInput);
        LevelEvent.Unload.BUS.addListener(clientCompatibilityManager::onDisconnect);
        ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(clientCompatibilityManager::onJoinServer);
        TickEvent.ServerTickEvent.Post.BUS.addListener(clientCompatibilityManager::onServer);

        FMLClientSetupEvent.getBus(context.getModBusGroup()).addListener(this::clientSetup);
        RegisterKeyMappingsEvent.BUS.addListener(((ForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE)::onRegisterKeyBinds);

        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
            if (OnboardingManager.isOnboarding()) {
                return OnboardingManager.getOnboardingScreen(parent);
            }
            return new VoiceChatSettingsScreen(parent);
        }));
    }

    public void clientSetup(FMLClientSetupEvent event) {
        initializeClient();
    }

    @Override
    public void initializeConfigs() {
        super.initializeConfigs();
        ConfigMigrator.migrateClientConfig();
    }

}
