package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

public class Config {

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final PlayerVolumeConfig VOLUME_CONFIG;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPairServer.getRight();
        SERVER = specPairServer.getLeft();

        Pair<ClientConfig, ForgeConfigSpec> specPairClient = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPairClient.getRight();
        CLIENT = specPairClient.getLeft();

        VOLUME_CONFIG = new PlayerVolumeConfig();
    }

    public static class ServerConfig {

        public final ForgeConfigSpec.IntValue VOICE_CHAT_PORT;
        public final ForgeConfigSpec.DoubleValue VOICE_CHAT_DISTANCE;
        public final ForgeConfigSpec.DoubleValue VOICE_CHAT_FADE_DISTANCE;
        public final ForgeConfigSpec.IntValue VOICE_CHAT_SAMPLE_RATE;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            VOICE_CHAT_PORT = builder
                    .comment("The port of the voice chat server")
                    .defineInRange("voice_chat.port", 24454, 0, 65535);
            VOICE_CHAT_DISTANCE = builder
                    .comment("The distance to where the voice can be heard")
                    .defineInRange("voice_chat.distance", 32D, 1D, 1_000_000D);
            VOICE_CHAT_FADE_DISTANCE = builder
                    .comment("The distance to where the voice starts fading")
                    .worldRestart()
                    .defineInRange("voice_chat.fade_distance", 16D, 1D, 1_000_000D);
            VOICE_CHAT_SAMPLE_RATE = builder
                    .comment("The sample rate for the voice chat")
                    .worldRestart()
                    .defineInRange("voice_chat.quality.sample_rate", 22050, 1000, 44100);
        }
    }

    public static class ClientConfig {

        public final ForgeConfigSpec.DoubleValue VOICE_CHAT_VOLUME;
        public final ForgeConfigSpec.DoubleValue VOICE_ACTIVATION_THRESHOLD;
        public final ForgeConfigSpec.DoubleValue MICROPHONE_AMPLIFICATION;
        public final ForgeConfigSpec.EnumValue<MicrophoneActivationType> MICROPHONE_ACTIVATION_TYPE;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            MICROPHONE_ACTIVATION_TYPE = builder
                    .comment("Microphone activation type")
                    .defineEnum("microphone_activation_type", MicrophoneActivationType.PTT);
            VOICE_ACTIVATION_THRESHOLD = builder
                    .comment("The threshold for voice activation in dB")
                    .defineInRange("voice_activation_threshold", -50D, -127D, 0D);
            VOICE_CHAT_VOLUME = builder
                    .comment("The voice chat volume")
                    .defineInRange("voice_chat_volume", 1D, 0D, 2D);
            MICROPHONE_AMPLIFICATION = builder
                    .comment("The voice chat microphone amplification")
                    .defineInRange("microphone_amplification", 1D, 0D, 4D);
        }
    }

    public static class PlayerVolumeConfig {
        private Properties properties;
        private Path path;

        public PlayerVolumeConfig() {
            path = FMLPaths.CONFIGDIR.get().resolve(Main.MODID).resolve("player-volumes.properties");
            properties = new Properties();
            try {
                load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void load() throws IOException {
            File file = path.toFile();
            if (file.exists()) {
                properties.load(new FileInputStream(file));
            }
        }

        public void save() throws IOException {
            File file = path.toFile();
            file.getParentFile().mkdirs();
            properties.store(new FileWriter(file, false), "The adjusted volumes for all other players");
        }

        public double getVolume(UUID uuid, double def) {
            String property = properties.getProperty(uuid.toString());
            if (property == null) {
                return setVolume(uuid, def);
            }
            try {
                return Double.parseDouble(property);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return setVolume(uuid, def);
            }
        }

        public double getVolume(PlayerEntity playerEntity) {
            return getVolume(playerEntity.getUniqueID(), 1D);
        }

        public double setVolume(UUID uuid, double value) {
            properties.put(uuid.toString(), String.valueOf(value));
            new Thread(() -> {
                try {
                    save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            return value;
        }
    }

}
