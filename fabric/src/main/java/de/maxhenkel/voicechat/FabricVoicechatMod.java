package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.integration.ViaVersionCompatibility;
import net.fabricmc.api.ModInitializer;

public class FabricVoicechatMod extends Voicechat implements ModInitializer {

    @Override
    public void onInitialize() {
        initialize();
        ViaVersionCompatibility.register();
    }

}
