package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.integration.ViaVersionCompatibility;
import net.fabricmc.api.ModInitializer;
import org.quiltmc.loader.api.ModContainer;

public class QuiltVoicechatMod extends Voicechat implements ModInitializer {

    @Override
    public void onInitialize() {
        initialize();
        ViaVersionCompatibility.register();
    }

    @Override
    public Loader getLoader() {
        return Loader.QUILT;
    }

}
