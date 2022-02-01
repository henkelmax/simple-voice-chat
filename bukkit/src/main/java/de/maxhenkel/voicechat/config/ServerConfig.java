package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;

public class ServerConfig {

    public final ConfigEntry<Integer> voiceChatPort;
    public final ConfigEntry<String> voiceChatBindAddress;
    public final ConfigEntry<Double> voiceChatDistance;
    public final ConfigEntry<Double> voiceChatFadeDistance;
    public ConfigEntry<Double> crouchDistanceMultiplier;
    public ConfigEntry<Double> whisperDistanceMultiplier;
    public final ConfigEntry<Codec> voiceChatCodec;
    public final ConfigEntry<Integer> voiceChatMtuSize;
    public final ConfigEntry<Integer> keepAlive;
    public final ConfigEntry<Boolean> groupsEnabled;
    public final ConfigEntry<Boolean> openGroups;
    public final ConfigEntry<String> voiceHost;
    public final ConfigEntry<Boolean> allowRecording;
    public final ConfigEntry<Boolean> spectatorInteraction;
    public ConfigEntry<Boolean> spectatorPlayerPossession;
    public ConfigEntry<Boolean> forceVoiceChat;
    public ConfigEntry<Integer> loginTimeout;

    public ServerConfig(ConfigBuilder builder) {
        voiceChatPort = builder.integerEntry("port", 24454, -1, 65535);
        voiceChatBindAddress = builder.stringEntry("bind_address", "");
        voiceChatDistance = builder.doubleEntry("max_voice_distance", 48D, 1D, 1_000_000D);
        voiceChatFadeDistance = builder.doubleEntry("min_voice_distance", 4D, 1D, 1_000_000D);
        crouchDistanceMultiplier = builder.doubleEntry("crouch_distance_multiplier", 1D, 0.01D, 1D);
        whisperDistanceMultiplier = builder.doubleEntry("whisper_distance_multiplier", 0.5D, 0.01D, 1D);
        voiceChatCodec = builder.enumEntry("codec", Codec.VOIP);
        voiceChatMtuSize = builder.integerEntry("mtu_size", 1024, 256, 10000);
        keepAlive = builder.integerEntry("keep_alive", 1000, 1000, Integer.MAX_VALUE);
        groupsEnabled = builder.booleanEntry("enable_groups", true);
        openGroups = builder.booleanEntry("open_groups", false);
        voiceHost = builder.stringEntry("voice_host", "");
        allowRecording = builder.booleanEntry("allow_recording", true);
        spectatorInteraction = builder.booleanEntry("spectator_interaction", false);
        spectatorPlayerPossession = builder.booleanEntry("spectator_player_possession", false);
        forceVoiceChat = builder.booleanEntry("force_voice_chat", false);
        loginTimeout = builder.integerEntry("login_timeout", 10_000, 100, Integer.MAX_VALUE);
    }

    public enum Codec {
        VOIP, AUDIO, RESTRICTED_LOWDELAY;
    }

}