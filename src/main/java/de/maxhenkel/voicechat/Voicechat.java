package de.maxhenkel.voicechat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.maxhenkel.voicechat.config.ConfigBuilder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.LoginPluginAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

public final class Voicechat extends JavaPlugin {

    public static Voicechat INSTANCE;

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static int COMPATIBILITY_VERSION = -1;

    public static MinecraftKey INIT = new MinecraftKey(MODID, "init");

    public static ServerConfig SERVER_CONFIG;
    private static ProtocolManager PROTOCOL_MANAGER;
    private PacketAdapter packetAdapter;

    @Override
    public void onEnable() {
        INSTANCE = this;
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("compatibility.properties");
            Properties props = new Properties();
            props.load(in);
            COMPATIBILITY_VERSION = Integer.parseInt(props.getProperty("compatibility_version"));
            LOGGER.info("Compatibility version {}", COMPATIBILITY_VERSION);
        } catch (Exception e) {
            LOGGER.error("Failed to read compatibility version");
        }

        ConfigBuilder.create(getDataFolder().toPath().resolve("voicechat-server.properties"), builder -> SERVER_CONFIG = new ServerConfig(builder));
        PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();

        packetAdapter = new PacketAdapter(this, ListenerPriority.HIGHEST, PacketType.Login.Client.START, PacketType.Login.Client.CUSTOM_PAYLOAD) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Login.Client.START) {
                    Player player = event.getPlayer();
                    byte[] payload = ByteBuffer.allocate(4).putInt(COMPATIBILITY_VERSION).array();
                    PacketContainer packet = LoginPluginAPI.instance().generatePluginRequest(INIT, payload);

                    try {
                        PROTOCOL_MANAGER.sendServerPacket(player, packet);
                    } catch (Exception e) {
                        LOGGER.error("Failed to send packet: {}", e.getMessage());
                    }
                } else if (event.getPacketType() == PacketType.Login.Client.CUSTOM_PAYLOAD) {
                    FriendlyByteBuf payload = LoginPluginAPI.instance().readPluginResponse(event.getPacket());
                    //payload is null in case of vanilla client
                    if (payload == null) {

                        event.setCancelled(true);
                        return;
                    }
                    int clientCompatibilityVersion = payload.readInt();

                    if (clientCompatibilityVersion != COMPATIBILITY_VERSION) {
                        Player player = event.getPlayer();
                        LOGGER.warn("Client {} has incompatible voice chat version (server={}, client={})", player.getName(), COMPATIBILITY_VERSION, clientCompatibilityVersion); //TODO check if player name is available at that point
                        if (clientCompatibilityVersion <= 6) {
                            // Send a literal string, as we don't know if the translations exist on these versions
                            disconnect(player, Component.text("Your voice chat version is not compatible with the servers version."));
                        } else {
                            // This translation key is only available for compatibility version 7+
                            disconnect(player, Component.translatable("message.voicechat.incompatible"));
                        }
                    }
                    event.setCancelled(true);
                }
            }
        };
        PROTOCOL_MANAGER.addPacketListener(packetAdapter);
    }

    public void disconnect(Player player, Component message) {
        PacketContainer packet = new PacketContainer(PacketType.Login.Server.DISCONNECT);
        packet.getChatComponents().write(0, WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(message)));
        try {
            PROTOCOL_MANAGER.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        PROTOCOL_MANAGER.removePacketListener(packetAdapter);
    }
}
