package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.integration.clothconfig.ClothConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FabricVoicechatClientMod extends VoicechatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        initializeClient();

        ClothConfig.init();
    }

}
