package de.maxhenkel.voicechat.net;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

public abstract class ClientServerNetManager extends NetManager {

    public static void sendToServer(Packet<?> packet) {
        NetHandlerPlayClient connection = Minecraft.getMinecraft().getConnection();
        if (connection != null && connection.getNetworkManager().isChannelOpen()) {
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            packet.toBytes(buffer);
            connection.sendPacket(new CPacketCustomPayload(packet.getIdentifier().toString(), buffer));
        }
    }

    public interface ClientReceiver<T extends Packet<T>> {
        void onPacket(Minecraft client, NetHandlerPlayClient handler, T packet);
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
