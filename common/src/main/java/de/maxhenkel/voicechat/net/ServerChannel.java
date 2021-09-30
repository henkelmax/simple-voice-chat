package de.maxhenkel.voicechat.net;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.ArrayList;
import java.util.List;

public class ServerChannel<T extends Packet<T>> {

    private final List<NetManager.ServerReceiver<T>> listeners;

    public ServerChannel() {
        listeners = new ArrayList<>();
    }

    public void registerServerListener(NetManager.ServerReceiver<T> packetReceiver) {
        listeners.add(packetReceiver);
    }

    public void onPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, T packet) {
        listeners.forEach(receiver -> receiver.onPacket(server, player, handler, packet));
    }

    public List<NetManager.ServerReceiver<T>> getListeners() {
        return listeners;
    }
}
