package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;
import de.maxhenkel.voicechat.Voicechat;

public class ServerConfig {

    public final ConfigEntry<Integer> voiceChatPort;
    public final ConfigEntry<String> voiceChatBindAddress;
    public final ConfigEntry<Double> voiceChatDistance;
    public ConfigEntry<Double> crouchDistanceMultiplier;
    public ConfigEntry<Double> whisperDistanceMultiplier;
    public final ConfigEntry<Codec> voiceChatCodec;
    public final ConfigEntry<Integer> voiceChatMtuSize;
    public final ConfigEntry<Integer> keepAlive;
    public final ConfigEntry<Boolean> groupsEnabled;
    public final ConfigEntry<String> voiceHost;
    public final ConfigEntry<Boolean> allowRecording;
    public final ConfigEntry<Boolean> spectatorInteraction;
    public ConfigEntry<Boolean> spectatorPlayerPossession;
    public ConfigEntry<Boolean> forceVoiceChat;
    public ConfigEntry<Integer> loginTimeout;
    public ConfigEntry<Double> broadcastRange;

    public ServerConfig(ConfigBuilder builder) {
        builder.header(String.format("Simple Voice Chat server config v%s", Voicechat.INSTANCE.getDescription().getVersion()));

        voiceChatPort = builder
                .integerEntry("port", 24454, -1, 65535,
                        "The port of the voice chat server",
                        "Setting this to \"-1\" sets the port to the Minecraft servers port (Not recommended)"
                );
        voiceChatBindAddress = builder
                .stringEntry("bind_address", "",
                        "The IP address to bind the voice chat server on",
                        "Leave empty to use 'server-ip' of server.properties",
                        "To bind to the wildcard address, use '*'"
                );
        voiceChatDistance = builder
                .doubleEntry("max_voice_distance", 48D, 1D, 1_000_000D,
                        "The distance to where the voice can be heard"
                );
        crouchDistanceMultiplier = builder
                .doubleEntry("crouch_distance_multiplier", 1D, 0.01D, 1D,
                        "The multiplier the voice distance will be reduced by when sneaking"
                );
        whisperDistanceMultiplier = builder
                .doubleEntry("whisper_distance_multiplier", 0.5D, 0.01D, 1D,
                        "The multiplier the voice distance will be reduced by when whispering"
                );
        voiceChatCodec = builder
                .enumEntry("codec", Codec.VOIP,
                        "The opus codec"
                );
        voiceChatMtuSize = builder
                .integerEntry("mtu_size", 1024, 256, 10000,
                        "The maximum size in bytes in a voice packet",
                        "Set this to a lower value if your voice packets don't arrive"
                );
        keepAlive = builder
                .integerEntry("keep_alive", 1000, 1000, Integer.MAX_VALUE,
                        "The frequency in which keep alive packets are sent",
                        "Setting this to a higher value may result in timeouts"
                );
        groupsEnabled = builder
                .booleanEntry("enable_groups", true,
                        "If group chats are allowed"
                );
        voiceHost = builder
                .stringEntry("voice_host", "",
                        "The host name that clients should use to connect to the voice chat",
                        "This may also include a port, e.g. 'example.com:24454'",
                        "Don't change this value if you don't know what you are doing"
                );
        allowRecording = builder
                .booleanEntry("allow_recording", true,
                        "If players are allowed to record the voice chat"
                );
        spectatorInteraction = builder
                .booleanEntry("spectator_interaction", false,
                        "If spectators are allowed to talk to other players"
                );
        spectatorPlayerPossession = builder
                .booleanEntry("spectator_player_possession", false,
                        "If spectators can talk to players they are spectating"
                );
        forceVoiceChat = builder
                .booleanEntry("force_voice_chat", false,
                        "If players without the mod should get kicked from the server"
                );
        loginTimeout = builder
                .integerEntry("login_timeout", 10_000, 100, Integer.MAX_VALUE,
                        "The amount of milliseconds, the server should wait to check if the player has the mod installed",
                        "Only active when force_voice_chat is set to true"
                );
        broadcastRange = builder
                .doubleEntry("broadcast_range", -1D, -1D, Double.MAX_VALUE,
                        "The range where the voice chat should broadcast audio to",
                        "A value <0 means 'max_voice_distance'"
                );
    }

    public enum Codec {
        VOIP, AUDIO, RESTRICTED_LOWDELAY;
    }

}