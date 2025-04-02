package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.networking.api.PayloadTypeRegistry;
import org.quiltmc.qsl.networking.api.server.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

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
        ClientServerChannel<T> c = new ClientServerChannel<>();
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
                ServerPlayNetworking.registerGlobalReceiver(type, (server, player, handler, payload, responseSender) -> {
                    try {
                        if (!Voicechat.SERVER.isCompatible(player) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        c.onServerPacket(player, payload);
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to process packet", e);
                    }
                });
            }
            if (toClient) {
                PayloadTypeRegistry.playS2C().register(type, codec);
                if (MinecraftQuiltLoader.getEnvironmentType().equals(EnvType.CLIENT)) {
                    ClientPlayNetworking.registerGlobalReceiver(type, (minecraft, handler, payload, responseSender) -> {
                        try {
                            Minecraft.getInstance().execute(() -> c.onClientPacket(minecraft.player, payload));
                        } catch (Exception e) {
                            Voicechat.LOGGER.error("Failed to register packet receiver", e);
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
    protected void sendToServerInternal(Packet<?> packet) {
        ClientPlayNetworking.send(packet);
    }

    @Override
    public void sendToClient(Packet<?> packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }

}
