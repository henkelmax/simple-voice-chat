package de.maxhenkel.voicechat.net;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class NetManager {

    public static <T extends Packet<T>> void registerServerReceiver(Class<T> packetType, ServerReceiver<T> packetReceiver) {
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            ServerPlayNetworking.registerGlobalReceiver(dummyPacket.getID(), (server, player, handler, buf, responseSender) -> {
                try {
                    T packet = packetType.getDeclaredConstructor().newInstance();
                    packet.fromBytes(buf);
                    packetReceiver.onPacket(server, player, handler, responseSender, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void sendToServer(Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ClientPlayNetworking.send(packet.getID(), buffer);
    }

    public static <T extends Packet<T>> void registerClientReceiver(Class<T> packetType, ClientReceiver<T> packetReceiver) {
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            ClientPlayNetworking.registerGlobalReceiver(dummyPacket.getID(), (client, handler, buf, responseSender) -> {
                try {
                    T packet = packetType.getDeclaredConstructor().newInstance();
                    packet.fromBytes(buf);
                    client.execute(() -> packetReceiver.onPacket(client, handler, responseSender, packet));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void sendToClient(ServerPlayer player, Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ServerPlayNetworking.send(player, packet.getID(), buffer);
    }

    public static interface ServerReceiver<T extends Packet<T>> {
        void onPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, PacketSender responseSender, T packet);
    }

    public static interface ClientReceiver<T extends Packet<T>> {
        void onPacket(Minecraft client, ClientPacketListener handler, PacketSender responseSender, T packet);
    }

}
