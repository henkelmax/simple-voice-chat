package de.maxhenkel.voicechat.net;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetManager extends NetManager {

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            if (toServer) {
                ServerPlayNetworking.registerGlobalReceiver(dummyPacket.getIdentifier(), (server, player, handler, buf, responseSender) -> {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(buf);
                        c.onServerPacket(server, player, handler, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            if (toClient && FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
                ClientPlayNetworking.registerGlobalReceiver(dummyPacket.getIdentifier(), (client, handler, buf, responseSender) -> {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(buf);
                        client.execute(() -> c.onClientPacket(client, handler, packet));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }

    @Override
    public void sendToServer(Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ClientPlayNetworking.send(packet.getIdentifier(), buffer);
    }

    @Override
    public void sendToClient(ServerPlayer player, Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ServerPlayNetworking.send(player, packet.getIdentifier(), buffer);
    }

}
