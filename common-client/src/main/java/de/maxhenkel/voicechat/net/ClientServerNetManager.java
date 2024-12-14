package de.maxhenkel.voicechat.net;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;

public abstract class ClientServerNetManager extends NetManager {

    public static void sendToServer(Packet<?> packet) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        packet.toBytes(buffer);
        ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
        if (connection != null && connection.getLevel() != null) {
            connection.send(new CCustomPayloadPacket(packet.getIdentifier(), buffer));
        }
    }

    public interface ClientReceiver<T extends Packet<T>> {
        void onPacket(Minecraft client, ClientPlayNetHandler handler, T packet);
    }

    public static <T extends Packet<T>> void setClientListener(Channel<T> channel, ClientServerNetManager.ClientReceiver<T> packetReceiver) {
        if (channel instanceof ClientServerChannel) {
            ClientServerChannel<T> c = (ClientServerChannel<T>) channel;
            c.setClientListener(packetReceiver);
        } else {
            throw new IllegalStateException("Channel is not a ClientServerChannel");
        }
    }

}
