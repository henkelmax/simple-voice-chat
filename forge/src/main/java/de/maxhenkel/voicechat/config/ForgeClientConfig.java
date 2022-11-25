package de.maxhenkel.voicechat.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeClientConfig extends ClientConfig {

    public ForgeClientConfig(ForgeConfigSpec.Builder builder) {
        super(new ForgeConfigBuilderWrapper(builder));
    }

}
