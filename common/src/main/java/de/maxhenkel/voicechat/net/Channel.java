package de.maxhenkel.voicechat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class Channel<T extends Packet<T>> {

    @Nullable
    private NetManager.ClientReceiver<T> clientListener;
    @Nullable
    private NetManager.ServerReceiver<T> serverListener;

    public Channel() {

    }

    public void setClientListener(NetManager.ClientReceiver<T> packetReceiver) {
        clientListener = packetReceiver;
    }

    public void setServerListener(NetManager.ServerReceiver<T> packetReceiver) {
        serverListener = packetReceiver;
    }

    public void onClientPacket(LocalPlayer player, T packet) {
        Minecraft.getInstance().execute(() -> {
            if (clientListener != null) {
                clientListener.onPacket(player, packet);
            }
        });
    }

    public void onServerPacket(ServerPlayer player, T packet) {
        player.getServer().execute(() -> {
            if (serverListener != null) {
                serverListener.onPacket(player, packet);
            }
        });
    }

}
