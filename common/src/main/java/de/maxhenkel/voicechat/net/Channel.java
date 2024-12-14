package de.maxhenkel.voicechat.net;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import javax.annotation.Nullable;

public class Channel<T extends Packet<T>> {

    @Nullable
    private NetManager.ServerReceiver<T> serverListener;

    public Channel() {

    }

    public void setServerListener(NetManager.ServerReceiver<T> packetReceiver) {
        serverListener = packetReceiver;
    }

    public void onServerPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, T packet) {
        server.execute(() -> {
            if (serverListener != null) {
                serverListener.onPacket(server, player, handler, packet);
            }
        });
    }

}
