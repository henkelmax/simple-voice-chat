package de.maxhenkel.voicechat.net;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetManager {

    public static <T extends Packet<T>> void registerServerReceiver(Class<T> packetType, ServerReceiver<T> packetReceiver) {
        try {
            T dummyPacket = packetType.newInstance();
            ServerPlayNetworking.registerGlobalReceiver(dummyPacket.getID(), (server, player, handler, buf, responseSender) -> {
                try {
                    T packet = packetType.newInstance();
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
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ClientPlayNetworking.send(packet.getID(), buffer);
    }

    public static <T extends Packet<T>> void registerClientReceiver(Class<T> packetType, ClientReceiver<T> packetReceiver) {
        try {
            T dummyPacket = packetType.newInstance();
            ClientPlayNetworking.registerGlobalReceiver(dummyPacket.getID(), (client, handler, buf, responseSender) -> {
                try {
                    T packet = packetType.newInstance();
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

    public static void sendToClient(ServerPlayerEntity player, Packet<?> packet) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ServerPlayNetworking.send(player, packet.getID(), buffer);
    }

    public static interface ServerReceiver<T extends Packet<T>> {
        void onPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketSender responseSender, T packet);
    }

    public static interface ClientReceiver<T extends Packet<T>> {
        void onPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketSender responseSender, T packet);
    }

}
