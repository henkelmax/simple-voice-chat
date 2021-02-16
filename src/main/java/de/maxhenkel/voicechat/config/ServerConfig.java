package de.maxhenkel.voicechat.config;

public class ServerConfig {

    public final ConfigBuilder.ConfigEntry<Integer> voiceChatPort;
    public final ConfigBuilder.ConfigEntry<String> voiceChatBindAddress;
    public final ConfigBuilder.ConfigEntry<Double> voiceChatDistance;
    public final ConfigBuilder.ConfigEntry<Double> voiceChatFadeDistance;
    public final ConfigBuilder.ConfigEntry<Integer> voiceChatSampleRate;
    public final ConfigBuilder.ConfigEntry<Integer> voiceChatMtuSize;

    public ServerConfig(ConfigBuilder builder) {
        voiceChatPort = builder.integerEntry("port", 24454, 0, 65535);
        voiceChatBindAddress = builder.stringEntry("bind_address", "");
        voiceChatDistance = builder.doubleEntry("voice_distance", 32D, 1D, 1_000_000D);
        voiceChatFadeDistance = builder.doubleEntry("voice_fade_distance", 16D, 1D, 1_000_000D);
        voiceChatSampleRate = builder.integerEntry("sample_rate", 16000, 10000, 44100);
        voiceChatMtuSize = builder.integerEntry("mtu_size", 900, 256, 10000);
    }

}
