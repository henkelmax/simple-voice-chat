package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.net.InitPacket;
import de.maxhenkel.voicechat.net.Packets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public class ServerVoiceEvents {

    private Server server;

    public ServerVoiceEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarting);
        PlayerEvents.PLAYER_LOGGED_IN.register(this::playerLoggedIn);
        PlayerEvents.PLAYER_LOGGED_OUT.register(this::playerLoggedOut);
    }

    public void serverStarting(MinecraftServer mcServer) {
        if (server != null) {
            server.close();
            server = null;
        }
        if (mcServer instanceof MinecraftDedicatedServer) {
            try {
                server = new Server(Voicechat.SERVER_CONFIG.voiceChatPort.get(), mcServer);
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playerLoggedIn(ServerPlayerEntity player) {
        if (server == null) {
            return;
        }

        UUID secret = server.getSecret(player.getUuid());
        InitPacket packet = new InitPacket(secret, Voicechat.SERVER_CONFIG.voiceChatPort.get(), Voicechat.SERVER_CONFIG.voiceChatSampleRate.get(), Voicechat.SERVER_CONFIG.voiceChatDistance.get(), Voicechat.SERVER_CONFIG.voiceChatFadeDistance.get());
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        packet.toBytes(buffer);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Packets.SECRET, buffer);
        Voicechat.LOGGER.info("Sent secret to " + player.getDisplayName().getString());
    }

    public void playerLoggedOut(ServerPlayerEntity player) {
        if (server == null) {
            return;
        }

        server.disconnectClient(player.getUuid());
        Voicechat.LOGGER.info("Disconnecting client " + player.getDisplayName().getString());
    }

    @Nullable
    public Server getServer() {
        return server;
    }
}
