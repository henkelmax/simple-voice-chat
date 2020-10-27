package de.maxhenkel.voicechat;

import de.maxhenkel.corelib.config.ConfigBase;
import de.maxhenkel.voicechat.voice.client.AudioChannelConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ServerConfig extends ConfigBase {

    public final ForgeConfigSpec.IntValue voiceChatPort;
    public final ForgeConfigSpec.DoubleValue voiceChatDistance;
    public final ForgeConfigSpec.DoubleValue voiceChatFadeDistance;
    public final ForgeConfigSpec.IntValue voiceChatSampleRate;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);

        voiceChatPort = builder
                .comment("The port of the voice chat server")
                .worldRestart()
                .defineInRange("voice_chat.port", 24454, 0, 65535);
        voiceChatDistance = builder
                .comment("The distance to where the voice can be heard")
                .worldRestart()
                .defineInRange("voice_chat.distance", 32D, 1D, 1_000_000D);
        voiceChatFadeDistance = builder
                .comment("The distance to where the voice starts fading")
                .worldRestart()
                .defineInRange("voice_chat.fade_distance", 16D, 1D, 1_000_000D);
        voiceChatSampleRate = builder
                .comment("The sample rate for the voice chat")
                .worldRestart()
                .defineInRange("voice_chat.sample_rate", 16000, 1000, 44100);
    }

    @Override
    public void onReload(ModConfig.ModConfigEvent event) {
        super.onReload(event);
        AudioChannelConfig.onServerConfigUpdate();
    }

}
