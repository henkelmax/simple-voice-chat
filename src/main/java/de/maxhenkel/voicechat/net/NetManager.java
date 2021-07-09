package de.maxhenkel.voicechat.net;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.LoginPluginAPI;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayInCustomPayload;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class NetManager {

    private static PacketAdapter packetAdapter;

    public static void onEnable() {
        packetAdapter = new PacketAdapter(Voicechat.INSTANCE, ListenerPriority.HIGHEST, PacketType.Login.Client.START, PacketType.Login.Client.CUSTOM_PAYLOAD, PacketType.Play.Client.CUSTOM_PAYLOAD) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (event.getPacketType() == PacketType.Login.Client.START) {
                    try {
                        byte[] payload = ByteBuffer.allocate(4).putInt(Voicechat.COMPATIBILITY_VERSION).array();
                        PacketContainer p = LoginPluginAPI.generatePluginRequest(Voicechat.INIT, payload);
                        Voicechat.PROTOCOL_MANAGER.sendServerPacket(player, p);
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to send packet: {}", e.getMessage());
                    }
                } else if (event.getPacketType() == PacketType.Login.Client.CUSTOM_PAYLOAD) {
                    onLoginCustomPayload(event);
                } else if (event.getPacketType() == PacketType.Play.Client.CUSTOM_PAYLOAD) {
                    onPlayCustomPayload(event);
                }
            }
        };
        Voicechat.PROTOCOL_MANAGER.addPacketListener(packetAdapter);
    }

    public static void onDisable() {
        Voicechat.PROTOCOL_MANAGER.removePacketListener(packetAdapter);
    }

    private static void onLoginCustomPayload(PacketEvent event) {
        Player player = event.getPlayer();
        FriendlyByteBuf buf = LoginPluginAPI.readPluginResponse(event);

        if (buf == null) {
            return;
        }

        int clientCompatibilityVersion = buf.readInt();

        if (clientCompatibilityVersion != Voicechat.COMPATIBILITY_VERSION) {
            Voicechat.LOGGER.warn("Client {} has incompatible voice chat version (server={}, client={})", player.getName(), Voicechat.COMPATIBILITY_VERSION, clientCompatibilityVersion); //TODO check if player name is available at that point
            disconnect(player, getIncompatibleMessage(clientCompatibilityVersion));
        }
    }

    public static Component getIncompatibleMessage(int clientCompatibilityVersion) {
        if (clientCompatibilityVersion <= 6) {
            // Send a literal string, as we don't know if the translations exist on these versions
            return Component.text(String.format(Voicechat.translate("not_compatible"), Voicechat.INSTANCE.getDescription().getVersion(), "Simple Voice Chat"));
        } else {
            // This translation key is only available for compatibility version 7+
            return Component.translatable("message.voicechat.incompatible_version",
                    Component.text(Voicechat.INSTANCE.getDescription().getVersion()).toBuilder().decorate(TextDecoration.BOLD).build(),
                    Component.text("Simple Voice Chat").toBuilder().decorate(TextDecoration.BOLD).build());
        }
    }

    private static void onPlayCustomPayload(PacketEvent event) {
        Player player = event.getPlayer();
        PacketPlayInCustomPayload customPayload = (PacketPlayInCustomPayload) event.getPacket().getHandle();
        MinecraftKey id = new MinecraftKey(customPayload.b().getNamespace(), customPayload.b().getKey());
        if (id.getFullKey().equals(PlayerStatePacket.PLAYER_STATE.getFullKey())) {
            ByteBuf buf = (ByteBuf) event.getPacket().getModifier().withType(PacketDataSerializer.class).read(0);
            FriendlyByteBuf payload = new FriendlyByteBuf(buf);
            PlayerStatePacket packet = new PlayerStatePacket();
            packet.fromBytes(payload);
            Voicechat.SERVER.getServer().getPlayerStateManager().onPlayerStatePacket(player, packet);
            event.setCancelled(true);
        } else if (id.getFullKey().equals(RequestSecretPacket.REQUEST_SECRET.getFullKey())) {
            ByteBuf buf = (ByteBuf) event.getPacket().getModifier().withType(PacketDataSerializer.class).read(0);
            FriendlyByteBuf payload = new FriendlyByteBuf(buf);
            RequestSecretPacket packet = new RequestSecretPacket();
            packet.fromBytes(payload);
            onRequestSecretPacket(player, packet);
            event.setCancelled(true);
        }
    }

    public static void onRequestSecretPacket(Player player, RequestSecretPacket packet) {
        Voicechat.LOGGER.info("Received secret request of {}", player.getName());
        if (packet.getCompatibilityVersion() != Voicechat.COMPATIBILITY_VERSION) {
            Voicechat.LOGGER.warn("Connected client {} has incompatible voice chat version (server={}, client={})", player.getName(), Voicechat.COMPATIBILITY_VERSION, packet.getCompatibilityVersion());
            disconnect(player, getIncompatibleMessage(packet.getCompatibilityVersion()));
            return;
        }
        Voicechat.SERVER.initializePlayerConnection(player);
    }

    public static PacketContainer createPacket(PacketType type, MinecraftKey id, FriendlyByteBuf buf) {
        PacketContainer packet = Voicechat.PROTOCOL_MANAGER.createPacket(type);
        packet.getMinecraftKeys().write(0, id);
        PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
        packetDataSerializer.writeBytes(buf);
        packet.getModifier().withType(PacketDataSerializer.class).write(0, packetDataSerializer);
        return packet;
    }

    public static void sendToClient(Player player, Packet<?> p) {
        try {
            FriendlyByteBuf buf = new FriendlyByteBuf();
            p.toBytes(buf);
            Voicechat.PROTOCOL_MANAGER.sendServerPacket(player, createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD, p.getID(), buf));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendMessage(Player player, Component component) {
        PacketContainer packet = Voicechat.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.CHAT);
        packet.getChatComponents().write(0, WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component)));
        packet.getChatTypes().write(0, EnumWrappers.ChatType.CHAT);
        packet.getUUIDs().write(0, new UUID(0L, 0L));
        try {
            Voicechat.PROTOCOL_MANAGER.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect(Player player, Component message) {
        PacketContainer packet = new PacketContainer(PacketType.Login.Server.DISCONNECT);
        packet.getChatComponents().write(0, WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(message)));
        try {
            Voicechat.PROTOCOL_MANAGER.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TemporaryPlayerFactory.getInjectorFromPlayer(player).getSocket().close();
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to disconnect player: {}", e.getMessage());
        }
    }
}