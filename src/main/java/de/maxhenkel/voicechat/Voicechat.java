package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.command.TestConnectionCommand;
import de.maxhenkel.voicechat.config.ConfigBuilder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Properties;

public class Voicechat implements ModInitializer {

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerVoiceEvents SERVER;
    @Nullable
    public static ServerConfig SERVER_CONFIG;

    public static final Identifier INIT = new Identifier(Voicechat.MODID, "init");
    public static int COMPATIBILITY_VERSION = -1;


    @Override
    public void onInitialize() {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("compatibility.properties");
            Properties props = new Properties();
            props.load(in);
            COMPATIBILITY_VERSION = Integer.parseInt(props.getProperty("compatibility_version"));
            LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);
        } catch (Exception e) {
            LOGGER.error("Failed to read compatibility version");
        }

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (server instanceof MinecraftDedicatedServer) {
                ConfigBuilder.create(server.getRunDirectory().toPath().resolve("config").resolve(MODID).resolve("voicechat-server.properties"), builder -> SERVER_CONFIG = new ServerConfig(builder));
            }
        });

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            buffer.writeInt(COMPATIBILITY_VERSION);
            sender.sendPacket(INIT, buffer);
        });
        ServerLoginNetworking.registerGlobalReceiver(INIT, (server, handler, understood, buf, synchronizer, responseSender) -> {
            if (!understood) {
                //Let vanilla clients pass, but not incompatible voice chat clients
                return;
            }

            int clientCompatibilityVersion = buf.readInt();

            if (clientCompatibilityVersion != Voicechat.COMPATIBILITY_VERSION) {
                Voicechat.LOGGER.warn("Client {} has incompatible voice chat version (server={}, client={})", handler.connection.getAddress(), Voicechat.COMPATIBILITY_VERSION, clientCompatibilityVersion);
                handler.disconnect(new TranslatableText("message.voicechat.incompatible_version"));
            }
        });

        SERVER = new ServerVoiceEvents();

        CommandRegistrationCallback.EVENT.register(TestConnectionCommand::register);
    }
}
