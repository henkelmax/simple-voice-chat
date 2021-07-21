package de.maxhenkel.voicechat;

import de.maxhenkel.corelib.config.ConfigBase;
import de.maxhenkel.opus4j.Opus;
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
    public final ForgeConfigSpec.EnumValue<Codec> voiceChatCodec;
    public final ForgeConfigSpec.IntValue voiceChatMtuSize;
    public final ForgeConfigSpec.IntValue keepAlive;
    public final ForgeConfigSpec.BooleanValue groupsEnabled;
    public final ForgeConfigSpec.BooleanValue openGroups;
    public final ForgeConfigSpec.ConfigValue<String> voiceHost;
    public final ForgeConfigSpec.BooleanValue allowRecording;

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
        voiceChatCodec = builder
                .comment("The opus codec")
                .defineEnum("voice_chat.codec", Codec.VOIP);
        voiceChatMtuSize = builder
                .comment("The maximum size in bytes in a voice packet", "Set this to a lower value if your voice packets don't arrive")
                .defineInRange("voice_chat.mtu_size", 1024, 256, 10000);
        keepAlive = builder
                .comment("The frequency in which keep alive packets are sent", "Setting this to a higher value may result in timeouts")
                .defineInRange("voice_chat.keep_alive", 1000, 1000, Integer.MAX_VALUE);
        groupsEnabled = builder
                .comment("If group chats are allowed")
                .define("voice_chat.enable_groups", true);
        openGroups = builder
                .comment("If players in group chats can be heard locally")
                .define("voice_chat.open_groups", false);
        voiceHost = builder
                .comment("The host name that clients should use to connect to the voice chat", "Don't change this value if you don't know what you are doing")
                .define("voice_chat.voice_host", "");
        allowRecording = builder
                .comment("If players are allowed to record the voice chat")
                .define("voice_chat.allow_recording", true);
    }

    @Override
    public void onReload(ModConfig.ModConfigEvent event) {
        super.onReload(event);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AudioChannelConfig::onServerConfigUpdate);
    }

    public enum Codec {
        VOIP(Opus.OPUS_APPLICATION_VOIP), AUDIO(Opus.OPUS_APPLICATION_AUDIO), RESTRICTED_LOWDELAY(Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY);

        private final int value;

        Codec(int value) {
            this.value = value;
        }

        public int getOpusValue() {
            return value;
        }
    }

}
