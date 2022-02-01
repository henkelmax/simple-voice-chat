package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.Config;
import de.maxhenkel.configbuilder.ConfigEntry;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ForgeServerConfig extends ServerConfig {

    public ForgeServerConfig(ForgeConfigSpec.Builder builder) {

        voiceChatPort = wrapConfigEntry(builder
                .worldRestart()
                .comment("The port of the voice chat server")
                .defineInRange("voice_chat.port", 24454, -1, 65535)
        );
        voiceChatBindAddress = wrapConfigEntry(builder
                .worldRestart()
                .comment("The IP address to bind the voice chat server on", "Leave empty to bind to an IP address chosen by the kernel")
                .define("voice_chat.bind_address", "")
        );
        voiceChatDistance = wrapConfigEntry(builder
                .worldRestart()
                .comment("The distance to where the voice can be heard")
                .defineInRange("voice_chat.max_voice_distance", 48D, 1D, 1_000_000D)
        );
        voiceChatFadeDistance = wrapConfigEntry(builder
                .worldRestart()
                .comment("The distance to where the voice starts fading")
                .defineInRange("voice_chat.min_voice_distance", 4D, 1D, 1_000_000D)
        );
        crouchDistanceMultiplier = wrapConfigEntry(builder
                .worldRestart()
                .comment("The multiplier the voice distance will be reduced by when sneaking")
                .defineInRange("voice_chat.crouch_distance_multiplier", 1D, 0.01D, 1D)
        );
        whisperDistanceMultiplier = wrapConfigEntry(builder
                .worldRestart()
                .comment("The multiplier the voice distance will be reduced by when whispering")
                .defineInRange("voice_chat.whisper_distance_multiplier", 0.5D, 0.01D, 1D)
        );
        voiceChatCodec = wrapConfigEntry(builder
                .worldRestart()
                .comment("The opus codec")
                .defineEnum("voice_chat.codec", Codec.VOIP)
        );
        voiceChatMtuSize = wrapConfigEntry(builder
                .worldRestart()
                .comment("The maximum size in bytes in a voice packet", "Set this to a lower value if your voice packets don't arrive")
                .defineInRange("voice_chat.mtu_size", 1024, 256, 10000)
        );
        keepAlive = wrapConfigEntry(builder
                .worldRestart()
                .comment("The frequency in which keep alive packets are sent", "Setting this to a higher value may result in timeouts")
                .defineInRange("voice_chat.keep_alive", 1000, 1000, Integer.MAX_VALUE)
        );
        groupsEnabled = wrapConfigEntry(builder
                .worldRestart()
                .comment("If group chats are allowed")
                .define("voice_chat.enable_groups", true)
        );
        openGroups = wrapConfigEntry(builder
                .worldRestart()
                .comment("If players in group chats can be heard locally")
                .define("voice_chat.open_groups", false)
        );
        voiceHost = wrapConfigEntry(builder
                .worldRestart()
                .comment("The host name that clients should use to connect to the voice chat", "Don't change this value if you don't know what you are doing")
                .define("voice_chat.voice_host", "")
        );
        allowRecording = wrapConfigEntry(builder
                .worldRestart()
                .comment("If players are allowed to record the voice chat")
                .define("voice_chat.allow_recording", true)
        );
        spectatorInteraction = wrapConfigEntry(builder
                .worldRestart()
                .comment("If spectators are allowed to talk to other players")
                .define("voice_chat.spectator_interaction", false)
        );
        spectatorPlayerPossession = wrapConfigEntry(builder
                .worldRestart()
                .comment("If spectators can talk to players they are spectating")
                .define("voice_chat.spectator_player_possession", false)
        );
        forceVoiceChat = wrapConfigEntry(builder
                .worldRestart()
                .comment("If players without the mod should get kicked from the server")
                .define("voice_chat.force_voice_chat", false)
        );
        loginTimeout = wrapConfigEntry(builder
                .worldRestart()
                .comment("The amount of milliseconds, the server should wait to check if the player has the mod installed", "Only active when force_voice_chat is set to true")
                .defineInRange("voice_chat.login_timeout", 10_000, 100, Integer.MAX_VALUE)
        );
    }

    public static <T> ConfigEntry<T> wrapConfigEntry(ForgeConfigSpec.ConfigValue<T> configValue) {
        return new ConfigEntry<T>() {
            @Override
            public T get() {
                return configValue.get();
            }

            @Override
            public ConfigEntry<T> set(T t) {
                configValue.set(t);
                return this;
            }

            @Override
            public ConfigEntry<T> reset() {
                throw new UnsupportedOperationException("Can't reset Forge config value");
            }

            @Override
            public ConfigEntry<T> save() {
                configValue.save();
                return this;
            }

            @Override
            public ConfigEntry<T> saveSync() {
                throw new UnsupportedOperationException("Can't synchronously save Forge config value");
            }

            @Override
            public T getDefault() {
                throw new UnsupportedOperationException("Cannot get default config value");
            }

            @Override
            public Config getConfig() {
                return fromBuilder(configValue.next());
            }
        };
    }

    public static Config fromBuilder(ForgeConfigSpec.Builder builder) {
        Map<String, Object> entries;
        try {
            Field field = builder.getClass().getDeclaredField("storage");
            com.electronwill.nightconfig.core.Config config = (com.electronwill.nightconfig.core.Config) field.get(builder);
            entries = config.valueMap();
        } catch (Exception e) {
            entries = new HashMap<>();
        }
        Map<String, Object> finalEntries = entries;
        return () -> finalEntries;
    }

}
