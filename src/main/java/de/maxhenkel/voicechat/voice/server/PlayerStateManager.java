package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager implements Listener {

    private final ConcurrentHashMap<UUID, PlayerState> states;

    public PlayerStateManager() {
        states = new ConcurrentHashMap<>();
    }

    public void onPlayerStatePacket(Player player, PlayerStatePacket packet) {
        PlayerState oldState = states.get(player.getUniqueId());

        PlayerState state = packet.getPlayerState();
        state.setGameProfile(((CraftPlayer) player).getProfile());
        if (oldState != null) {
            state.setGroup(oldState.getGroup());
        } else {
            state.setGroup(null);
        }
        states.put(player.getUniqueId(), state);
        broadcastState(state);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    private void broadcastState(PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        Voicechat.INSTANCE.getServer().getOnlinePlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    public void onPlayerCompatibilityCheckSucceded(Player player) {
        PlayerState state = states.getOrDefault(player.getUniqueId(), defaultDisconnectedState(player));
        states.put(player.getUniqueId(), state);
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        NetManager.sendToClient(player, packet);
    }

    private void removePlayer(Player player) {
        states.remove(player.getUniqueId());
        broadcastState(new PlayerState(true, true, ((CraftPlayer) player).getProfile()));
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(Player player) {
        return new PlayerState(false, true, ((CraftPlayer) player).getProfile());
    }

    public void setGroup(Player player, @Nullable ClientGroup group) {
        PlayerState state = states.get(player.getUniqueId());
        if (state == null) {
            state = PlayerStateManager.defaultDisconnectedState(player);
        }
        state.setGroup(group);
        states.put(player.getUniqueId(), state);
        broadcastState(state);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
