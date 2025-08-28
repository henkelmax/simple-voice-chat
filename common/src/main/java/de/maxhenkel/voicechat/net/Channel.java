package de.maxhenkel.voicechat.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public class Channel<T extends Packet<T>> {

    @Nullable
    private NetManager.ServerReceiver<T> serverListener;

    public Channel() {

    }

    public void setServerListener(NetManager.ServerReceiver<T> packetReceiver) {
        serverListener = packetReceiver;
    }

    public void onServerPacket(MinecraftServer server, EntityPlayerMP player, NetHandlerPlayServer handler, T packet) {
        CommonCompatibilityManager.INSTANCE.execute(server, () -> {
            if (serverListener != null) {
                serverListener.onPacket(server, player, handler, packet);
            }
        });
    }

}
