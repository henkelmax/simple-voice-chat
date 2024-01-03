package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import javax.annotation.Nullable;
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
    public <T extends Packet<T>> void register(RegisterPayloadHandlerEvent event) {
        IPayloadRegistrar registrar = event.registrar(Voicechat.MODID).optional();
        for (PacketRegister<T> register : packets) {
            register(registrar, register.channel(), register.packetClass(), register.toClient(), register.toServer());
        }
    }

    private <T extends Packet<T>> void register(IPayloadRegistrar registrar, Channel<T> channel, Class<T> packetType, boolean toClient, boolean toServer) {
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();

            registrar.play(
                    dummyPacket.getIdentifier(),
                    buf -> {
                        try {
                            return WrappedPacket.read(packetType, buf);
                        } catch (Exception e) {
                            return null;
                        }
                    },
                    (payload, context) -> {
                        if (payload == null) {
                            return;
                        }
                        @Nullable Player p = context.player().orElse(null);
                        context.workHandler().execute(() -> {
                            if (toServer && context.flow().equals(PacketFlow.SERVERBOUND)) {
                                if (!(p instanceof ServerPlayer sender)) {
                                    return;
                                }
                                try {
                                    if (!Voicechat.SERVER.isCompatible(sender) && !packetType.equals(RequestSecretPacket.class)) {
                                        return;
                                    }
                                    channel.onServerPacket(sender.server, sender, sender.connection, (T) payload.packet());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    onClientPacket(channel, (T) payload.packet());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
            ).optional();
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
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        PacketDistributor.SERVER.noArg().send(new WrappedPacket<>(packet));
    }

    @Override
    public void sendToClient(Packet<?> packet, ServerPlayer player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        PacketDistributor.PLAYER.with(player).send(new WrappedPacket<>(packet));
    }

    @OnlyIn(Dist.CLIENT)
    private <T extends Packet<T>> void onClientPacket(Channel<T> channel, T packet) {
        channel.onClientPacket(Minecraft.getInstance(), Minecraft.getInstance().getConnection(), packet);
    }

    private record WrappedPacket<T extends Packet<T>>(Packet<T> packet) implements CustomPacketPayload {

        public static <T extends Packet<T>> WrappedPacket<T> read(Class<T> packetClass, FriendlyByteBuf buf) throws Exception {
            T packet = packetClass.getDeclaredConstructor().newInstance();
            packet.fromBytes(buf);
            return new WrappedPacket<>(packet);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            packet.toBytes(buf);
        }

        @Override
        public ResourceLocation id() {
            return packet.getIdentifier();
        }
    }

    record PacketRegister<T extends Packet<T>>(Class<T> packetClass, Channel<T> channel, boolean toClient,
                                               boolean toServer) {

    }

}
