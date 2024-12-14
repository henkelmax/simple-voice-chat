package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

public abstract class ClientServerNetManager extends NetManager {

    public static void sendToServer(Packet<?> packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null && connection.getLevel() != null) {
            CommonCompatibilityManager.INSTANCE.getNetManager().sendToServerInternal(packet);
        }
    }

    public interface ClientReceiver<T extends Packet<T>> {
        void onPacket(LocalPlayer player, T packet);
    }

    public static <T extends Packet<T>> void setClientListener(Channel<T> channel, ClientServerNetManager.ClientReceiver<T> packetReceiver) {
        if (channel instanceof ClientServerChannel<T> c) {
            c.setClientListener(packetReceiver);
        } else {
            throw new IllegalStateException("Channel is not a ClientServerChannel");
        }
    }

}
