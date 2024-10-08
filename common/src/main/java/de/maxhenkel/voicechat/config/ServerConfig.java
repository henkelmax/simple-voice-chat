package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.opus4j.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;

public class ServerConfig {

    public ConfigEntry<Integer> voiceChatPort;
    public ConfigEntry<String> voiceChatBindAddress;
    public ConfigEntry<Double> voiceChatDistance;
    public ConfigEntry<Double> crouchDistanceMultiplier;
    public ConfigEntry<Double> whisperDistanceMultiplier;
    public ConfigEntry<Codec> voiceChatCodec;
    public ConfigEntry<Integer> voiceChatMtuSize;
    public ConfigEntry<Integer> keepAlive;
    public ConfigEntry<Boolean> groupsEnabled;
    public ConfigEntry<String> voiceHost;
    public ConfigEntry<Boolean> allowRecording;
    public ConfigEntry<Boolean> spectatorInteraction;
    public ConfigEntry<Boolean> spectatorPlayerPossession;
    public ConfigEntry<Boolean> forceVoiceChat;
    public ConfigEntry<Integer> loginTimeout;
    public ConfigEntry<Double> broadcastRange;
    public ConfigEntry<Boolean> allowPings;

    public ServerConfig(ConfigBuilder builder) {

        builder.header(String.format("%s server config v%s", CommonCompatibilityManager.INSTANCE.getModName(), CommonCompatibilityManager.INSTANCE.getModVersion()));

        voiceChatPort = builder
                .integerEntry("port", 24454, -1, 65535,
                        "The port number to use for the voice chat communication.",
                        "Audio packets are always transmitted via the UDP protocol on the port number",
                        "specified here, independently of other networking used for the game server.",
                        "Set this to '-1' to use the same port number that is used by the Minecraft server.",
                        "However, it is strongly recommended NOT to use the same port number because UDP on",
                        "it is also used by default for the server query. Doing so may crash the server!"
                );
        voiceChatBindAddress = builder
                .stringEntry("bind_address", "",
                        "The server IP address to bind the voice chat to",
                        "Leave blank to use the 'server-ip' property from the 'server.properties' config file",
                        "To bind to the wildcard IP address, use '*'"
                );
        voiceChatDistance = builder
                .doubleEntry("max_voice_distance", 48D, 1D, 1_000_000D,
                        "The distance to which the voice can be heard"
                );
        crouchDistanceMultiplier = builder
                .doubleEntry("crouch_distance_multiplier", 1D, 0.01D, 1D,
                        "The multiplier of the voice distance when crouching"
                );
        whisperDistanceMultiplier = builder
                .doubleEntry("whisper_distance_multiplier", 0.5D, 0.01D, 1D,
                        "The multiplier of the voice distance when whispering"
                );
        voiceChatCodec = builder
                .enumEntry("codec", Codec.VOIP,
                        "The Opus codec",
                        "Valid values are 'VOIP', 'AUDIO', and 'RESTRICTED_LOWDELAY'"
                );
        voiceChatMtuSize = builder
                .integerEntry("mtu_size", 1024, 256, 10000,
                        "The maximum size that audio packets are allowed to have (in bytes)",
                        "Set this to a lower value if audio packets don't arrive"
                );
        keepAlive = builder
                .integerEntry("keep_alive", 1000, 1000, Integer.MAX_VALUE,
                        "The frequency at which keep-alive packets are sent (in milliseconds)",
                        "Setting this to a higher value may result in timeouts"
                );
        groupsEnabled = builder
                .booleanEntry("enable_groups", true,
                        "If group chats are allowed"
                );
        voiceHost = builder
                .stringEntry("voice_host", "",
                        "The hostname that clients should use to connect to the voice chat",
                        "This may also include a port, e.g. 'example.com:24454'",
                        "Do NOT change this value if you don't know what you're doing"
                );
        allowRecording = builder
                .booleanEntry("allow_recording", true,
                        "If players are allowed to record the voice chat audio"
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
                        "If players without the voice chat mod should be kicked from the server"
                );
        loginTimeout = builder
                .integerEntry("login_timeout", 10_000, 100, Integer.MAX_VALUE,
                        "The amount of time the server should wait to check if a player has the mod installed (in milliseconds)",
                        "Only relevant when 'force_voice_chat' is set to 'true'"
                );
        broadcastRange = builder
                .doubleEntry("broadcast_range", -1D, -1D, Double.MAX_VALUE,
                        "The range in which the voice chat should broadcast audio",
                        "A value less than 0 means 'max_voice_distance'"
                );
        allowPings = builder
                .booleanEntry("allow_pings", true,
                        "If the voice chat server should reply to external pings"
                );
    }

    public static enum Codec {
        VOIP(OpusEncoder.Application.VOIP, OpusEncoderMode.VOIP), AUDIO(OpusEncoder.Application.AUDIO, OpusEncoderMode.AUDIO), RESTRICTED_LOWDELAY(OpusEncoder.Application.LOW_DELAY, OpusEncoderMode.RESTRICTED_LOWDELAY);

        private final OpusEncoder.Application application;
        private final OpusEncoderMode mode;

        Codec(OpusEncoder.Application application, OpusEncoderMode mode) {
            this.application = application;
            this.mode = mode;
        }

        public OpusEncoder.Application getApplication() {
            return application;
        }

        public OpusEncoderMode getMode() {
            return mode;
        }
    }

}