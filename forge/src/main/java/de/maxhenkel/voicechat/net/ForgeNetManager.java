package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.*;

import java.util.HashMap;
import java.util.Map;

public class ForgeNetManager extends NetManager {

    private final Map<ResourceLocation, net.minecraftforge.network.Channel<FriendlyByteBuf>> channels;

    public ForgeNetManager() {
        channels = new HashMap<>();
    }

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            EventNetworkChannel channel = ChannelBuilder.named(dummyPacket.type().id())
                    .acceptedVersions((status, version) -> true)
                    .optional()
                    .networkProtocolVersion(Voicechat.COMPATIBILITY_VERSION)
                    .eventNetworkChannel();
            channel.addListener(event -> {
                if (event.getPayload() == null) {
                    return;
                }
                CustomPayloadEvent.Context context = event.getSource();
                if (toServer && context.isServerSide()) {
                    try {
                        if (!Voicechat.SERVER.isCompatible(context.getSender()) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(event.getPayload());
                        c.onServerPacket(context.getSender(), packet);
                        context.setPacketHandled(true);
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to process packet", e);
                    }
                } else {
                    if (isSameThread()) {
                        return;
                    }
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(event.getPayload());
                        onClientPacket(c, packet);
                        context.setPacketHandled(true);
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to process packet", e);
                    }
                }
            });
            channels.put(dummyPacket.type().id(), channel);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void sendToServerInternal(Packet<?> packet) {
        net.minecraftforge.network.Channel<FriendlyByteBuf> channel = channels.get(packet.type().id());
        if (channel == null) {
            throw new IllegalArgumentException("No channel for packet %s".formatted(packet.type().id()));
        }

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);

        channel.send(buffer, PacketDistributor.SERVER.noArg());
    }

    @Override
    public void sendToClient(Packet<?> packet, ServerPlayer player) {
        net.minecraftforge.network.Channel<FriendlyByteBuf> channel = channels.get(packet.type().id());
        if (channel == null) {
            throw new IllegalArgumentException("No channel for packet %s".formatted(packet.type().id()));
        }

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);

        channel.send(buffer, PacketDistributor.PLAYER.with(player));
    }

    @OnlyIn(Dist.CLIENT)
    private <T extends Packet<T>> void onClientPacket(Channel<T> channel, T packet) {
        channel.onClientPacket(Minecraft.getInstance().player, packet);
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isSameThread() {
        return Minecraft.getInstance().isSameThread();
    }

}
