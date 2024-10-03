package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeNetManager extends NetManager {

    private List<PacketRegister> packets;

    public NeoForgeNetManager() {
        packets = new ArrayList<>();
    }

    @Override
    public void init() {
        packets.clear();
        super.init();
    }

    @SubscribeEvent
    public <T extends Packet<T>> void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Voicechat.MODID).optional();
        for (PacketRegister<T> register : packets) {
            register(registrar, register.channel(), register.packetClass(), register.toClient(), register.toServer());
        }
    }

    private <T extends Packet<T>> void register(PayloadRegistrar registrar, Channel<T> channel, Class<T> packetType, boolean toClient, boolean toServer) {
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();

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

            IPayloadHandler<T> handler = (payload, context) -> {
                context.enqueueWork(() -> {
                    if (toServer && context.flow().equals(PacketFlow.SERVERBOUND)) {
                        if (!(context.player() instanceof ServerPlayer sender)) {
                            return;
                        }
                        try {
                            if (!Voicechat.SERVER.isCompatible(sender) && !packetType.equals(RequestSecretPacket.class)) {
                                return;
                            }
                            channel.onServerPacket(sender, payload);
                        } catch (Exception e) {
                            Voicechat.LOGGER.error("Failed to process packet", e);
                        }
                    } else {
                        if (!(context.player() instanceof LocalPlayer player)) {
                            return;
                        }
                        try {
                            onClientPacket(player, channel, payload);
                        } catch (Exception e) {
                            Voicechat.LOGGER.error("Failed to process packet", e);
                        }
                    }
                });
            };

            if (toClient && toServer) {
                registrar.playBidirectional(dummyPacket.type(), codec, handler);
            } else if (toClient) {
                registrar.playToClient(dummyPacket.type(), codec, handler);
            } else if (toServer) {
                registrar.playToServer(dummyPacket.type(), codec, handler);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        packets.add(new PacketRegister<>(packetType, c, toClient, toServer));
        return c;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void sendToServerInternal(Packet<?> packet) {
        try {
            PacketDistributor.sendToServer(packet);
        } catch (UnsupportedOperationException e) {
            if (Voicechat.debugMode()) {
                Voicechat.LOGGER.warn("Server does not accept voice chat packets", e);
            } else {
                Voicechat.LOGGER.warn("Server does not accept voice chat packets");
            }
        }
    }

    @Override
    public void sendToClient(Packet<?> packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @OnlyIn(Dist.CLIENT)
    private <T extends Packet<T>> void onClientPacket(LocalPlayer player, Channel<T> channel, T packet) {
        channel.onClientPacket(player, packet);
    }

    record PacketRegister<T extends Packet<T>>(Class<T> packetClass, Channel<T> channel, boolean toClient,
                                               boolean toServer) {

    }

}
