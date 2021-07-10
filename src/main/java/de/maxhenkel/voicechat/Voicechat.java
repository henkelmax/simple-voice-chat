package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.command.VoicechatCommands;
import de.maxhenkel.voicechat.config.ConfigBuilder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class Voicechat implements ModInitializer {

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerVoiceEvents SERVER;
    @Nullable
    public static ServerConfig SERVER_CONFIG;

    public static final ResourceLocation INIT = new ResourceLocation(Voicechat.MODID, "init");
    public static int COMPATIBILITY_VERSION = -1;

    public static final Pattern GROUP_REGEX = Pattern.compile("^\\S[^\"\\n\\r\\t]{0,15}$");

    @Override
    public void onInitialize() {
        try {
            COMPATIBILITY_VERSION = readCompatibilityVersion();
            LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);
        } catch (Exception e) {
            LOGGER.error("Failed to read compatibility version");
        }

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (server instanceof DedicatedServer) {
                ConfigBuilder.create(server.getServerDirectory().toPath().resolve("config").resolve(MODID).resolve("voicechat-server.properties"), builder -> SERVER_CONFIG = new ServerConfig(builder));
            }
        });

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
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
                Voicechat.LOGGER.warn("Client {} has incompatible voice chat version (server={}, client={})", handler.connection.getRemoteAddress(), Voicechat.COMPATIBILITY_VERSION, clientCompatibilityVersion);
                handler.disconnect(getIncompatibleMessage(clientCompatibilityVersion));
            }
        });

        SERVER = new ServerVoiceEvents();

        CommandRegistrationCallback.EVENT.register(VoicechatCommands::register);
    }

    public static Component getIncompatibleMessage(int clientCompatibilityVersion) {
        if (clientCompatibilityVersion <= 6) {
            return new TextComponent("Your voice chat version is not compatible with the servers version.\nPlease install version ")
                    .append(new TextComponent(getModVersion()).withStyle(ChatFormatting.BOLD))
                    .append(" of ")
                    .append(new TextComponent(getModName()).withStyle(ChatFormatting.BOLD))
                    .append(".");
        } else {
            return new TranslatableComponent("message.voicechat.incompatible_version",
                    new TextComponent(getModVersion()).withStyle(ChatFormatting.BOLD),
                    new TextComponent(getModName()).withStyle(ChatFormatting.BOLD));
        }
    }

    private static String getModVersion() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MODID).orElse(null);
        if (modContainer == null) {
            return "N/A";
        }
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }

    private static String getModName() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MODID).orElse(null);
        if (modContainer == null) {
            return MODID;
        }
        return modContainer.getMetadata().getName();
    }

    private static int readCompatibilityVersion() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MODID).orElse(null);
        if (modContainer == null) {
            return -1;
        }
        return modContainer.getMetadata().getCustomValue(MODID).getAsObject().get("compatibilityVersion").getAsNumber().intValue();
    }

}
