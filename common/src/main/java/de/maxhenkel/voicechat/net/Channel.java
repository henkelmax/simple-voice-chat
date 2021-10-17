package de.maxhenkel.voicechat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

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

    public void onClientPacket(Minecraft client, ClientPacketListener handler, T packet) {
        if (clientListener != null) {
            clientListener.onPacket(client, handler, packet);
        }
    }

    public void onServerPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, T packet) {
        if (serverListener != null) {
            serverListener.onPacket(server, player, handler, packet);
        }
    }

}
