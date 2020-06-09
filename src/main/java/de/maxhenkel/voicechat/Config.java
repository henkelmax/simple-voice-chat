package de.maxhenkel.voicechat;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPairServer.getRight();
        SERVER = specPairServer.getLeft();

        Pair<ClientConfig, ForgeConfigSpec> specPairClient = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPairClient.getRight();
        CLIENT = specPairClient.getLeft();
    }

    public static class ServerConfig {

        public final ForgeConfigSpec.IntValue VOICE_CHAT_PORT;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            VOICE_CHAT_PORT = builder.defineInRange("voice_chat_port", 24454, 0, 65535);
        }
    }

    public static class ClientConfig {

        public ClientConfig(ForgeConfigSpec.Builder builder) {

        }
    }

}
