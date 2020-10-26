package de.maxhenkel.voicechat;

import de.maxhenkel.corelib.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig extends ConfigBase {

    public final ForgeConfigSpec.IntValue voiceChatPort;
    public final ForgeConfigSpec.DoubleValue voiceChatDistance;
    public final ForgeConfigSpec.DoubleValue voiceChatFadeDistance;
    public final ForgeConfigSpec.IntValue voiceChatSampleRate;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);

        voiceChatPort = builder
                .comment("The port of the voice chat server")
                .defineInRange("voice_chat.port", 24454, 0, 65535);
        voiceChatDistance = builder
                .comment("The distance to where the voice can be heard")
                .defineInRange("voice_chat.distance", 32D, 1D, 1_000_000D);
        voiceChatFadeDistance = builder
                .comment("The distance to where the voice starts fading")
                .worldRestart()
                .defineInRange("voice_chat.fade_distance", 16D, 1D, 1_000_000D);
        voiceChatSampleRate = builder
                .comment("The sample rate for the voice chat")
                .worldRestart()
                .defineInRange("voice_chat.quality.sample_rate", 22050, 1000, 44100);
    }
}
