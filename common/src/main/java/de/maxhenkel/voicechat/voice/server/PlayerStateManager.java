package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {

    private final ConcurrentHashMap<UUID, PlayerState> states;

    public PlayerStateManager() {
        states = new ConcurrentHashMap<>();
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(this::removePlayer);

        CommonCompatibilityManager.INSTANCE.getNetManager().playerStateChannel.registerServerListener((server, player, handler, packet) -> {
            PlayerState oldState = states.get(player.getUUID());

            PlayerState state = packet.getPlayerState();
            state.setGameProfile(player.getGameProfile());
            if (oldState != null) {
                state.setGroup(oldState.getGroup());
            } else {
                state.setGroup(null);
            }
            states.put(player.getUUID(), state);
            broadcastState(server, state);
            Voicechat.logDebug("Got state of {}: {}", player.getDisplayName().getString(), state);
        });
    }

    private void broadcastState(MinecraftServer server, PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        server.getPlayerList().getPlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    public void onPlayerCompatibilityCheckSucceded(ServerPlayer player) {
        PlayerState state = states.getOrDefault(player.getUUID(), defaultDisconnectedState(player));
        states.put(player.getUUID(), state);
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        NetManager.sendToClient(player, packet);
        Voicechat.logDebug("Setting initial state of {}: {}", player.getDisplayName().getString(), state);
    }

    private void removePlayer(ServerPlayer player) {
        states.remove(player.getUUID());
        broadcastState(player.server, new PlayerState(true, true, player.getGameProfile()));
        Voicechat.logDebug("Removing state of {}", player.getDisplayName().getString());
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(ServerPlayer player) {
        return new PlayerState(false, true, player.getGameProfile());
    }

    public void setGroup(MinecraftServer server, ServerPlayer player, @Nullable ClientGroup group) {
        PlayerState state = states.get(player.getUUID());
        if (state == null) {
            state = PlayerStateManager.defaultDisconnectedState(player);
            Voicechat.logDebug("Defaulting to default state for {}: {}", player.getDisplayName().getString(), state);
        }
        state.setGroup(group);
        states.put(player.getUUID(), state);
        broadcastState(server, state);
        Voicechat.logDebug("Setting group of {}: {}", player.getDisplayName().getString(), state);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
