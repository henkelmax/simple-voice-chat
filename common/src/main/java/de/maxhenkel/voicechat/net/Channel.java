package de.maxhenkel.voicechat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.ArrayList;
import java.util.List;

public class Channel<T extends Packet<T>> {

    private final List<NetManager.ClientReceiver<T>> clientListeners;
    private final List<NetManager.ServerReceiver<T>> serverListeners;

    public Channel() {
        clientListeners = new ArrayList<>();
        serverListeners = new ArrayList<>();
    }

    public void registerClientListener(NetManager.ClientReceiver<T> packetReceiver) {
        clientListeners.add(packetReceiver);
    }

    public void registerServerListener(NetManager.ServerReceiver<T> packetReceiver) {
        serverListeners.add(packetReceiver);
    }

    public void onClientPacket(Minecraft client, ClientPacketListener handler, T packet) {
        clientListeners.forEach(receiver -> receiver.onPacket(client, handler, packet));
    }

    public void onServerPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, T packet) {
        serverListeners.forEach(receiver -> receiver.onPacket(server, player, handler, packet));
    }

}
