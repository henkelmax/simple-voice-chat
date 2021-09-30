package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class ForgeNetManager extends NetManager {

    private final SimpleChannel channel;

    public ForgeNetManager() {
        channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Voicechat.MODID, "default"),
                () -> String.valueOf(Voicechat.COMPATIBILITY_VERSION),
                String.valueOf(Voicechat.COMPATIBILITY_VERSION)::equals,
                String.valueOf(Voicechat.COMPATIBILITY_VERSION)::equals
        );
    }

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            channel.registerMessage(dummyPacket.getID(), packetType, Packet::toBytes, friendlyByteBuf -> {
                try {
                    T packet = packetType.getDeclaredConstructor().newInstance();
                    return packet.fromBytes(friendlyByteBuf);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }, (t, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                if (toServer && context.getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
                    c.onServerPacket(context.getSender().server, context.getSender(), context.getSender().connection, t);
                } else if (toClient && context.getDirection().equals(NetworkDirection.PLAY_TO_CLIENT)) {
                    onClientPacket(c, t);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void sendToServer(Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        channel.sendToServer(packet);
    }

    @OnlyIn(Dist.CLIENT)
    private <T extends Packet<T>> void onClientPacket(Channel<T> channel, T packet) {
        channel.onClientPacket(Minecraft.getInstance(), Minecraft.getInstance().getConnection(), packet);
    }

    @Override
    public void sendToClient(ServerPlayer player, Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        channel.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

}
