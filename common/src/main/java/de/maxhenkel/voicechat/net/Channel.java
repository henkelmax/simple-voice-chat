package de.maxhenkel.voicechat.net;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class Channel<T extends Packet<T>> {

    @Nullable
    private NetManager.ServerReceiver<T> serverListener;

    public Channel() {

    }

    public void setServerListener(NetManager.ServerReceiver<T> packetReceiver) {
        serverListener = packetReceiver;
    }

    public void onServerPacket(ServerPlayer player, T packet) {
        player.getServer().execute(() -> {
            if (serverListener != null) {
                serverListener.onPacket(player, packet);
            }
        });
    }

}
