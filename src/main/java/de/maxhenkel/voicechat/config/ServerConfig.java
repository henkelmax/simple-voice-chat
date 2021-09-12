package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.ConfigBuilder;

public class ServerConfig {

    public final ConfigBuilder.ConfigEntry<Integer> voiceChatPort;
    public final ConfigBuilder.ConfigEntry<String> voiceChatBindAddress;
    public final ConfigBuilder.ConfigEntry<Double> voiceChatDistance;
    public final ConfigBuilder.ConfigEntry<Double> voiceChatFadeDistance;
    public final ConfigBuilder.ConfigEntry<Enum<Codec>> voiceChatCodec;
    public final ConfigBuilder.ConfigEntry<Integer> voiceChatMtuSize;
    public final ConfigBuilder.ConfigEntry<Integer> keepAlive;
    public final ConfigBuilder.ConfigEntry<Boolean> groupsEnabled;
    public final ConfigBuilder.ConfigEntry<Boolean> openGroups;
    public final ConfigBuilder.ConfigEntry<String> voiceHost;
    public final ConfigBuilder.ConfigEntry<Boolean> allowRecording;

    public ServerConfig(ConfigBuilder builder) {
        voiceChatPort = builder.integerEntry("port", 24454, 0, 65535);
        voiceChatBindAddress = builder.stringEntry("bind_address", "0.0.0.0");
        voiceChatDistance = builder.doubleEntry("voice_distance", 32D, 1D, 1_000_000D);
        voiceChatFadeDistance = builder.doubleEntry("voice_fade_distance", 16D, 1D, 1_000_000D);
        voiceChatCodec = builder.enumEntry("codec", Codec.VOIP);
        voiceChatMtuSize = builder.integerEntry("mtu_size", 1024, 256, 10000);
        keepAlive = builder.integerEntry("keep_alive", 1000, 1000, Integer.MAX_VALUE);
        groupsEnabled = builder.booleanEntry("enable_groups", true);
        openGroups = builder.booleanEntry("open_groups", false);
        voiceHost = builder.stringEntry("voice_host", "");
        allowRecording = builder.booleanEntry("allow_recording", true);
    }

    public enum Codec {
        VOIP, AUDIO, RESTRICTED_LOWDELAY;
    }

}