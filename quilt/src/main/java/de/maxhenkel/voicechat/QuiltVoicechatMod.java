package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.integration.ViaVersionCompatibility;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class QuiltVoicechatMod extends Voicechat implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {
        initialize();
        ViaVersionCompatibility.register();
    }

}
