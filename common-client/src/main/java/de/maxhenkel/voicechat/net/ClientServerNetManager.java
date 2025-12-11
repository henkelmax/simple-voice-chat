package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Packet;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

public abstract class ClientServerNetManager extends NetManager {

    public static void sendToServer(Packet<?> packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(Voicechat.outSourcing.byteBufOf(buffer));
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null && connection.getLevel() != null) {
            connection.send(new ServerboundCustomPayloadPacket(new ResourceLocation(Voicechat.MODID, packet.getID()), buffer));
        }
    }

    public interface ClientReceiver<T extends Packet<T>> {
        void onPacket(Minecraft client, ClientPacketListener handler, T packet);
    }

    public static <T extends Packet<T>> void setClientListener(Channel<T> channel, ClientServerNetManager.ClientReceiver<T> packetReceiver) {
        if (channel instanceof ClientServerChannel<T> c) {
            c.setClientListener(packetReceiver);
        } else {
            throw new IllegalStateException("Channel is not a ClientServerChannel");
        }
    }

}
