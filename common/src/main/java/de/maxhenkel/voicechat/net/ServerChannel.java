package de.maxhenkel.voicechat.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;

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

    public void onPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetHandler handler, T packet) {
        listeners.forEach(receiver -> receiver.onPacket(server, player, handler, packet));
    }

    public List<NetManager.ServerReceiver<T>> getListeners() {
        return listeners;
    }
}
