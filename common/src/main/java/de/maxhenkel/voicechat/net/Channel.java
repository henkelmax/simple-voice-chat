package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.text.TranslationTextComponent;

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
        if (!Voicechat.SERVER.getRateLimiter().allow(player.getUUID())) {
            Voicechat.LOGGER.warn("Player {} exceeded packet rate limit", player.getName().getString());
            player.connection.disconnect(new TranslationTextComponent("disconnect.exceeded_packet_rate"));
            return;
        }
        CommonCompatibilityManager.INSTANCE.execute(server, () -> {
            if (serverListener != null) {
                serverListener.onPacket(server, player, handler, packet);
            }
        });
    }

}
