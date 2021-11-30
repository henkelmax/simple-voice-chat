package de.maxhenkel.voicechat.net;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayInCustomPayload;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class NetManager {

    private static PacketAdapter packetAdapter;

    public static void onEnable() {
        packetAdapter = new PacketAdapter(Voicechat.INSTANCE, ListenerPriority.HIGHEST, PacketType.Play.Client.CUSTOM_PAYLOAD) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.CUSTOM_PAYLOAD) {
                    onPlayCustomPayload(event);
                }
            }
        };
        Voicechat.PROTOCOL_MANAGER.addPacketListener(packetAdapter);
    }

    public static void onDisable() {
        Voicechat.PROTOCOL_MANAGER.removePacketListener(packetAdapter);
    }

    private static void onPlayCustomPayload(PacketEvent event) {
        Player player = event.getPlayer();
        PacketPlayInCustomPayload customPayload = (PacketPlayInCustomPayload) event.getPacket().getHandle();
        MinecraftKey id = new MinecraftKey(customPayload.b().b(), customPayload.b().a());
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
            Voicechat.SERVER.onRequestSecretPacket(player, packet);
            event.setCancelled(true);
        } else if (id.getFullKey().equals(CreateGroupPacket.CREATE_GROUP.getFullKey())) {
            ByteBuf buf = (ByteBuf) event.getPacket().getModifier().withType(PacketDataSerializer.class).read(0);
            FriendlyByteBuf payload = new FriendlyByteBuf(buf);
            CreateGroupPacket packet = new CreateGroupPacket();
            packet.fromBytes(payload);
            Voicechat.SERVER.getServer().getGroupManager().onCreateGroupPacket(player, packet);
            event.setCancelled(true);
        } else if (id.getFullKey().equals(JoinGroupPacket.SET_GROUP.getFullKey())) {
            ByteBuf buf = (ByteBuf) event.getPacket().getModifier().withType(PacketDataSerializer.class).read(0);
            FriendlyByteBuf payload = new FriendlyByteBuf(buf);
            JoinGroupPacket packet = new JoinGroupPacket();
            packet.fromBytes(payload);
            Voicechat.SERVER.getServer().getGroupManager().onJoinGroupPacket(player, packet);
            event.setCancelled(true);
        } else if (id.getFullKey().equals(LeaveGroupPacket.LEAVE_GROUP.getFullKey())) {
            ByteBuf buf = (ByteBuf) event.getPacket().getModifier().withType(PacketDataSerializer.class).read(0);
            FriendlyByteBuf payload = new FriendlyByteBuf(buf);
            LeaveGroupPacket packet = new LeaveGroupPacket();
            packet.fromBytes(payload);
            Voicechat.SERVER.getServer().getGroupManager().onLeaveGroupPacket(player, packet);
            event.setCancelled(true);
        }
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
        if (!Voicechat.SERVER.isCompatible(player)) {
            return;
        }
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
}