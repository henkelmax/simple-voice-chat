package de.maxhenkel.voicechat;

import de.maxhenkel.corelib.config.ConfigBase;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig extends ConfigBase {

    public final ForgeConfigSpec.DoubleValue voiceChatVolume;
    public final ForgeConfigSpec.DoubleValue voiceActivationThreshold;
    public final ForgeConfigSpec.DoubleValue microphoneAmplification;
    public final ForgeConfigSpec.EnumValue<MicrophoneActivationType> microphoneActivationType;
    public final ForgeConfigSpec.IntValue outputBufferSize;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        super(builder);
        microphoneActivationType = builder
                .comment("Microphone activation type")
                .defineEnum("microphone_activation_type", MicrophoneActivationType.PTT);
        voiceActivationThreshold = builder
                .comment("The threshold for voice activation in dB")
                .defineInRange("voice_activation_threshold", -50D, -127D, 0D);
        voiceChatVolume = builder
                .comment("The voice chat volume")
                .defineInRange("voice_chat_volume", 1D, 0D, 2D);
        microphoneAmplification = builder
                .comment("The voice chat microphone amplification")
                .defineInRange("microphone_amplification", 1D, 0D, 4D);
        outputBufferSize = builder
                .comment(
                        "The size of the audio output buffer in packets",
                        "Higher values mean a higher latency, but less crackles",
                        "Increase this value if you have an unstable internet connection"
                )
                .defineInRange("output_buffer_size", 2, 1, 8);
    }

}
