package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.integration.ViaVersionCompatibility;
import de.maxhenkel.voicechat.integration.vanish.VanishIntegration;
import net.fabricmc.api.ModInitializer;

public class QuiltVoicechatMod extends Voicechat implements ModInitializer {

    @Override
    public void onInitialize() {
        initialize();
        ViaVersionCompatibility.register();
        VanishIntegration.init();
    }

    @Override
    public Loader getLoader() {
        return Loader.QUILT;
    }

}
