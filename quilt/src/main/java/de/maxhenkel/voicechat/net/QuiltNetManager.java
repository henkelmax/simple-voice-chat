package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;

import java.util.HashSet;
import java.util.Set;

public class QuiltNetManager extends NetManager {

    private final Set<ResourceLocation> packets;

    public QuiltNetManager() {
        packets = new HashSet<>();
    }

    public Set<ResourceLocation> getPackets() {
        return packets;
    }

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            ResourceLocation identifier = dummyPacket.getIdentifier();
            packets.add(identifier);

            if (toServer) {
                ServerPlayNetworking.registerGlobalReceiver(identifier, (ServerPlayNetworking.CustomChannelReceiver<PacketByteBufPayload>) (server, player, handler, payload, responseSender) -> {
                    try {
                        if (!Voicechat.SERVER.isCompatible(player) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(payload.data());
                        c.onServerPacket(server, player, handler, packet);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            if (toClient && !CommonCompatibilityManager.INSTANCE.isDedicatedServer()) {
                ClientPlayNetworking.registerGlobalReceiver(identifier, (ClientPlayNetworking.CustomChannelReceiver<PacketByteBufPayload>) (client, handler, payload, responseSender) -> {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(payload.data());
                        client.execute(() -> c.onClientPacket(client, handler, packet));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }

    @Override
    protected void sendToServerInternal(Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ClientPlayNetworking.send(packet.getIdentifier(), buffer);
    }

    @Override
    public void sendToClient(Packet<?> packet, ServerPlayer player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ServerPlayNetworking.send(player, packet.getIdentifier(), buffer);
    }

}
