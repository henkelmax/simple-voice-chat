package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class FabricNetManager extends NetManager {

    private final Set<ResourceLocation> packets;

    public FabricNetManager() {
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
            CustomPacketPayload.Type<T> type = dummyPacket.type();
            packets.add(type.id());
            StreamCodec<RegistryFriendlyByteBuf, T> codec = new StreamCodec<>() {

                @Override
                public void encode(RegistryFriendlyByteBuf buf, T packet) {
                    packet.toBytes(buf);
                }

                @Override
                public T decode(RegistryFriendlyByteBuf buf) {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(buf);
                        return packet;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            if (toServer) {
                PayloadTypeRegistry.playC2S().register(type, codec);
                ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                    try {
                        if (!Voicechat.SERVER.isCompatible(context.player()) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        c.onServerPacket(context.player(), payload);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            if (toClient) {
                PayloadTypeRegistry.playS2C().register(type, codec);
                if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
                    ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                        try {
                            Minecraft.getInstance().execute(() -> c.onClientPacket(context.player(), payload));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }

    @Override
    public void sendToServer(Packet<?> packet, ClientPacketListener connection) {
        ClientPlayNetworking.send(packet);
    }

    @Override
    public void sendToClient(Packet<?> packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }

}
