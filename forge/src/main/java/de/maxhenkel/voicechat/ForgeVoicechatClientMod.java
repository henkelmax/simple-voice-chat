package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler;

public class ForgeVoicechatClientMod extends VoicechatClient {

    private final ClientCompatibilityManager compatibilityManager;

    public ForgeVoicechatClientMod() {
        compatibilityManager = new ForgeClientCompatibilityManager();
        initializeClient();
        MinecraftForge.EVENT_BUS.register(compatibilityManager);
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((client, parent) -> {
            return new VoiceChatSettingsScreen(parent);
        }));
    }

    @Override
    public ClientCompatibilityManager createCompatibilityManager() {
        return compatibilityManager;
    }
}
