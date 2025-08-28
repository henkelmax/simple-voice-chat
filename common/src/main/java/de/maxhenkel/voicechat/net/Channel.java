package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;

import javax.annotation.Nullable;

public class Channel<T extends Packet<T>> {

    @Nullable
    private NetManager.ServerReceiver<T> serverListener;

    public Channel() {

    }

    public void setServerListener(NetManager.ServerReceiver<T> packetReceiver) {
        serverListener = packetReceiver;
    }

    public void onServerPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetHandler handler, T packet) {
        CommonCompatibilityManager.INSTANCE.execute(server, () -> {
            if (serverListener != null) {
                serverListener.onPacket(server, player, handler, packet);
            }
        });
    }

}
