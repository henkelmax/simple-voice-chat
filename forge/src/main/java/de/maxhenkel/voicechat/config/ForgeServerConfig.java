package de.maxhenkel.voicechat.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeServerConfig extends ServerConfig {

    public ForgeServerConfig(ForgeConfigSpec.Builder builder) {
        super(new ForgeConfigBuilderWrapper(builder, "voice_chat"));
    }

}
