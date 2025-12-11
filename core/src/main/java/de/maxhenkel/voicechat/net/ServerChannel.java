package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.ServerPlayer;

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

    public void onPacket(MinecraftServer server, ServerPlayer player, Object handler, T packet) {
        listeners.forEach(receiver -> receiver.onPacket(server, player, handler, packet));
    }

    public List<NetManager.ServerReceiver<T>> getListeners() {
        return listeners;
    }
}
