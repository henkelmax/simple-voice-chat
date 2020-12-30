package de.maxhenkel.voicechat;

import de.maxhenkel.corelib.config.ConfigBase;
import de.maxhenkel.voicechat.voice.client.AudioChannelConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.config.ModConfig;

public class ServerConfig extends ConfigBase {

    public final ForgeConfigSpec.IntValue voiceChatPort;
    public final ForgeConfigSpec.ConfigValue<String> voiceChatBindAddress;
    public final ForgeConfigSpec.DoubleValue voiceChatDistance;
    public final ForgeConfigSpec.DoubleValue voiceChatFadeDistance;
    public final ForgeConfigSpec.IntValue voiceChatSampleRate;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);

        voiceChatPort = builder
                .comment("The port of the voice chat server")
                .defineInRange("voice_chat.port", 24454, 0, 65535);
        voiceChatBindAddress = builder
                .comment("The IP address to bind the voice chat server on", "Leave empty to bind to an IP address chosen by the kernel")
                .define("voice_chat.bind_address", "");
        voiceChatDistance = builder
                .comment("The distance to where the voice can be heard")
                .defineInRange("voice_chat.distance", 32D, 1D, 1_000_000D);
        voiceChatFadeDistance = builder
                .comment("The distance to where the voice starts fading")
                .defineInRange("voice_chat.fade_distance", 16D, 1D, 1_000_000D);
        voiceChatSampleRate = builder
                .comment("The sample rate for the voice chat")
                .defineInRange("voice_chat.sample_rate", 16000, 10000, 44100);
    }

    @Override
    public void onReload(ModConfig.ModConfigEvent event) {
        super.onReload(event);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AudioChannelConfig::onServerConfigUpdate);

    }
}
