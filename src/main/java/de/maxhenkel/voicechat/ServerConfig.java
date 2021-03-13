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
    public final ForgeConfigSpec.IntValue voiceChatMtuSize;
    public final ForgeConfigSpec.IntValue keepAlive;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);

        voiceChatPort = builder
                .comment("The port of the voice chat server")
                .defineInRange("voice_chat.port", 24454, 0, 65535);
        voiceChatBindAddress = builder
                .comment("The IP address to bind the voice chat server on", "Use '0.0.0.0' to bind to an IP address chosen by the kernel")
                .define("voice_chat.bind_address", "0.0.0.0");
        voiceChatDistance = builder
                .comment("The distance to where the voice can be heard")
                .defineInRange("voice_chat.distance", 32D, 1D, 1_000_000D);
        voiceChatFadeDistance = builder
                .comment("The distance to where the voice starts fading")
                .defineInRange("voice_chat.fade_distance", 16D, 1D, 1_000_000D);
        voiceChatSampleRate = builder
                .comment("The sample rate for the voice chat")
                .defineInRange("voice_chat.sample_rate", 16000, 10000, 44100);
        voiceChatMtuSize = builder
                .comment("The maximum size in bytes in a voice packet", "Set this to a lower value if your voice packets don't arrive")
                .defineInRange("voice_chat.mtu_size", 900, 256, 10000);
        keepAlive = builder
                .comment("The frequency in which keep alive packets are sent", "Setting this to a higher value may result in timeouts")
                .defineInRange("voice_chat.keep_alive", 1000, 1000, Integer.MAX_VALUE);
    }

    @Override
    public void onReload(ModConfig.ModConfigEvent event) {
        super.onReload(event);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AudioChannelConfig::onServerConfigUpdate);

    }
}
