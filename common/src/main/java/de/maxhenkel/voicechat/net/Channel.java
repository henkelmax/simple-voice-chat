package de.maxhenkel.voicechat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;

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

    public void onClientPacket(Minecraft client, ClientPlayNetHandler handler, T packet) {
        if (clientListener != null) {
            clientListener.onPacket(client, handler, packet);
        }
    }

    public void onServerPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetHandler handler, T packet) {
        if (serverListener != null) {
            serverListener.onPacket(server, player, handler, packet);
        }
    }

}
