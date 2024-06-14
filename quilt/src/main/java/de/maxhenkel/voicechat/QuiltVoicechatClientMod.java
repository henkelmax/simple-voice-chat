package de.maxhenkel.voicechat;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import de.maxhenkel.voicechat.integration.clothconfig.ClothConfig;

public class QuiltVoicechatClientMod extends VoicechatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(ModContainer mod) {
        initializeClient();
        ClothConfig.init();
    }
}
