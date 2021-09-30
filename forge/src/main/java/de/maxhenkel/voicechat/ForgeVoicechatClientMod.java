package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraftforge.common.MinecraftForge;

public class ForgeVoicechatClientMod extends VoicechatClient {

    private final ClientCompatibilityManager compatibilityManager;

    public ForgeVoicechatClientMod() {
        compatibilityManager = new ForgeClientCompatibilityManager();
        initializeClient();
        MinecraftForge.EVENT_BUS.register(compatibilityManager);
    }

    @Override
    public ClientCompatibilityManager createCompatibilityManager() {
        return compatibilityManager;
    }
}
