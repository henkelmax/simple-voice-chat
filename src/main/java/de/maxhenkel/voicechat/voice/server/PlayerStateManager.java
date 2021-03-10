package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.corelib.net.NetUtils;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.net.PlayerStateMessage;
import de.maxhenkel.voicechat.net.PlayerStatesMessage;
import de.maxhenkel.voicechat.net.SetPlayerStateMessage;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStateManager {

    private Map<UUID, PlayerState> states;

    public PlayerStateManager() {
        states = new HashMap<>();
    }

    public void onPlayerStatePacket(ServerPlayerEntity player, SetPlayerStateMessage message) {
        states.put(player.getUUID(), message.getPlayerState());

        broadcastState(player.server, player.getUUID(), message.getPlayerState());
    }

    private void broadcastState(MinecraftServer server, UUID uuid, PlayerState state) {
        PlayerStateMessage msg = new PlayerStateMessage(uuid, state);
        server.getPlayerList().getPlayers().forEach(p -> {
            if (!p.getUUID().equals(uuid)) {
                NetUtils.sendTo(Main.SIMPLE_CHANNEL, p, msg);
            }
        });
    }

    public void onPlayerLoggedIn(ServerPlayerEntity player) {
        notifyPlayer(player);
    }

    public void onPlayerLoggedOut(ServerPlayerEntity player) {
        removePlayer(player);
    }

    private void notifyPlayer(ServerPlayerEntity player) {
        PlayerStatesMessage msg = new PlayerStatesMessage(states);
        NetUtils.sendTo(Main.SIMPLE_CHANNEL, player, msg);
    }

    private void removePlayer(ServerPlayerEntity player) {
        states.remove(player.getUUID());
        broadcastState(player.server, player.getUUID(), new PlayerState(true, true));
    }

}
