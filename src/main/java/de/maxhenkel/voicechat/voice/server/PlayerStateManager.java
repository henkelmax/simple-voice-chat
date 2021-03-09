package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.net.Packets;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStateManager {

    private Map<UUID, PlayerState> states;

    public PlayerStateManager() {
        states = new HashMap<>();
        PlayerEvents.PLAYER_LOGGED_OUT.register(this::removePlayer);
        PlayerEvents.PLAYER_LOGGED_IN.register(this::notifyPlayer);

        ServerPlayNetworking.registerGlobalReceiver(Packets.PLAYER_STATE, (server, player, handler, buf, responseSender) -> {
            PlayerStatePacket statePacket = PlayerStatePacket.fromBytes(buf);
            states.put(player.getUuid(), statePacket.getPlayerState());

            broadcastState(server, player.getUuid(), statePacket.getPlayerState());
        });
    }

    private void broadcastState(MinecraftServer server, UUID uuid, PlayerState state) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        PlayerStatePacket broadcastPacket = new PlayerStatePacket(uuid, state);
        broadcastPacket.toBytes(buffer);
        server.getPlayerManager().getPlayerList().forEach(p -> {
            if (!p.getUuid().equals(uuid)) {
                ServerPlayNetworking.send(p, Packets.PLAYER_STATE, buffer);
            }
        });
    }

    private void notifyPlayer(ServerPlayerEntity player) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        packet.toBytes(buffer);
        ServerPlayNetworking.send(player, Packets.PLAYER_STATES, buffer);
        broadcastState(player.server, player.getUuid(), new PlayerState(false, true));
    }

    private void removePlayer(ServerPlayerEntity player) {
        states.remove(player.getUuid());
        broadcastState(player.server, player.getUuid(), new PlayerState(true, true));
    }

}
