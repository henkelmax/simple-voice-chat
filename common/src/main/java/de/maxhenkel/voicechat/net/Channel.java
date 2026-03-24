package de.maxhenkel.voicechat.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

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
        if (!Voicechat.SERVER.getRateLimiter().allow(player.getUniqueID())) {
            Voicechat.LOGGER.warn("Player {} exceeded packet rate limit", player.getName());
            player.connection.disconnect(new TextComponentTranslation("disconnect.exceeded_packet_rate"));
            return;
        }
        CommonCompatibilityManager.INSTANCE.execute(server, () -> {
            if (serverListener != null) {
                serverListener.onPacket(server, player, handler, packet);
            }
        });
    }

}
