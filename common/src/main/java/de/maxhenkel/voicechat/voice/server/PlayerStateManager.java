package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {

    private ConcurrentHashMap<UUID, PlayerState> states;

    public PlayerStateManager() {
        states = new ConcurrentHashMap<>();
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(this::removePlayer);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedIn(this::notifyPlayer);

        CommonCompatibilityManager.INSTANCE.getNetManager().playerStateChannel.registerServerListener((server, player, handler, packet) -> {
            PlayerState state = packet.getPlayerState();
            state.setGameProfile(player.getGameProfile());
            states.put(player.getUUID(), state);
            broadcastState(server, state);
        });
    }

    private void broadcastState(MinecraftServer server, PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        server.getPlayerList().getPlayers().forEach(p -> CommonCompatibilityManager.INSTANCE.getNetManager().sendToClient(p, packet));
    }

    private void notifyPlayer(ServerPlayer player) {
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        CommonCompatibilityManager.INSTANCE.getNetManager().sendToClient(player, packet);
        broadcastState(player.server, new PlayerState(false, true, player.getGameProfile()));
    }

    private void removePlayer(ServerPlayer player) {
        states.remove(player.getUUID());
        broadcastState(player.server, new PlayerState(true, true, player.getGameProfile()));
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
