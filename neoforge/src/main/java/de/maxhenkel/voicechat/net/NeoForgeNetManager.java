package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.INetworkDirection;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.event.EventNetworkChannel;

public class NeoForgeNetManager extends NetManager {

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            EventNetworkChannel channel = NetworkRegistry.ChannelBuilder.named(dummyPacket.getIdentifier())
                    .networkProtocolVersion(() -> NetworkRegistry.ACCEPTVANILLA)
                    .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA))
                    .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA))
                    .eventNetworkChannel();
            channel.addListener(event -> {
                if (event.getPayload() == null) {
                    return;
                }
                NetworkEvent.Context context = event.getSource();
                if (toServer && context.getDirection().equals(PlayNetworkDirection.PLAY_TO_SERVER)) {
                    try {
                        if (!Voicechat.SERVER.isCompatible(context.getSender()) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(event.getPayload());
                        c.onServerPacket(context.getSender().server, context.getSender(), context.getSender().connection, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // TODO Check
                    /*if (isSameThread()) {
                        return;
                    }*/
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(event.getPayload());
                        onClientPacket(c, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }

    @Override
    public void sendToServer(Packet<?> packet, ClientPacketListener connection) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        connection.send(PlayNetworkDirection.PLAY_TO_SERVER.buildPacket(new INetworkDirection.PacketData(buffer, 0), packet.getIdentifier()));
    }

    @Override
    public void sendToClient(Packet<?> packet, ServerPlayer player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        player.connection.send(PlayNetworkDirection.PLAY_TO_CLIENT.buildPacket(new INetworkDirection.PacketData(buffer, 0), packet.getIdentifier()));
    }

    @OnlyIn(Dist.CLIENT)
    private <T extends Packet<T>> void onClientPacket(Channel<T> channel, T packet) {
        channel.onClientPacket(Minecraft.getInstance(), Minecraft.getInstance().getConnection(), packet);
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isSameThread() {
        return Minecraft.getInstance().isSameThread();
    }

}
