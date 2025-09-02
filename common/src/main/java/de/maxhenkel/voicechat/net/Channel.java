package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
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
        CommonCompatibilityManager.INSTANCE.execute(player.level().getServer(), () -> {
            if (serverListener != null) {
                serverListener.onPacket(player, packet);
            }
        });
    }

}
