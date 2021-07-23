package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.corelib.net.NetUtils;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.net.PlayerStateMessage;
import de.maxhenkel.voicechat.net.PlayerStatesMessage;
import de.maxhenkel.voicechat.net.SetPlayerStateMessage;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {

    private ConcurrentHashMap<UUID, PlayerState> states;

    public PlayerStateManager() {
        states = new ConcurrentHashMap<>();
    }

    public void onPlayerStatePacket(ServerPlayer player, SetPlayerStateMessage message) {
        PlayerState state = message.getPlayerState();
        state.setGameProfile(player.getGameProfile());
        states.put(player.getUUID(), state);
        broadcastState(player.server, state);
    }

    private void broadcastState(MinecraftServer server, PlayerState state) {
        PlayerStateMessage packet = new PlayerStateMessage(state);
        server.getPlayerList().getPlayers().forEach(p -> NetUtils.sendTo(Main.SIMPLE_CHANNEL, p, packet));
    }

    public void onPlayerLoggedIn(ServerPlayer player) {
        notifyPlayer(player);
    }

    public void onPlayerLoggedOut(ServerPlayer player) {
        removePlayer(player);
    }

    private void notifyPlayer(ServerPlayer player) {
        PlayerStatesMessage msg = new PlayerStatesMessage(states);
        NetUtils.sendTo(Main.SIMPLE_CHANNEL, player, msg);
        broadcastState(player.server, new PlayerState(false, true, player.getGameProfile()));
    }

    private void removePlayer(ServerPlayer player) {
        states.remove(player.getUUID());
        broadcastState(player.server, new PlayerState(true, true, player.getGameProfile())); //TODO maybe remove
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
