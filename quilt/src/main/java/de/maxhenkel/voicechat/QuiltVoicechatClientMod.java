package de.maxhenkel.voicechat;

import net.fabricmc.api.ClientModInitializer;
import de.maxhenkel.voicechat.integration.clothconfig.ClothConfig;

public class QuiltVoicechatClientMod extends VoicechatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        initializeClient();
        ClothConfig.init();
    }
}
