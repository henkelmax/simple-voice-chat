package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
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

        NetManager.registerServerReceiver(PlayerStatePacket.class, (server, player, handler, responseSender, packet) -> {
            states.put(player.getUuid(), packet.getPlayerState());
            broadcastState(server, player.getUuid(), packet.getPlayerState());
        });
    }

    private void broadcastState(MinecraftServer server, UUID uuid, PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(uuid, state);
        server.getPlayerManager().getPlayerList().forEach(p -> {
            if (!p.getUuid().equals(uuid)) {
                NetManager.sendToClient(p, packet);
            }
        });
    }

    private void notifyPlayer(ServerPlayerEntity player) {
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        NetManager.sendToClient(player, packet);
        broadcastState(player.server, player.getUuid(), new PlayerState(false, true));
    }

    private void removePlayer(ServerPlayerEntity player) {
        states.remove(player.getUuid());
        broadcastState(player.server, player.getUuid(), new PlayerState(true, true));
    }

}
