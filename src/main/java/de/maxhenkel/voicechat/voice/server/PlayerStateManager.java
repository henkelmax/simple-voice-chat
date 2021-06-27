package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager implements Listener {

    private ConcurrentHashMap<UUID, PlayerState> states;

    public PlayerStateManager() {
        states = new ConcurrentHashMap<>();
    }

    public void onPlayerStatePacket(Player player, PlayerStatePacket packet) {
        PlayerState state = packet.getPlayerState();
        state.setGameProfile(((CraftPlayer) player).getProfile());
        states.put(player.getUniqueId(), state);
        broadcastState(state);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        notifyPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    private void broadcastState(PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        Voicechat.INSTANCE.getServer().getOnlinePlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    private void notifyPlayer(Player player) {
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        NetManager.sendToClient(player, packet);
        broadcastState(new PlayerState(false, true, ((CraftPlayer) player).getProfile()));
    }

    private void removePlayer(Player player) {
        states.remove(player.getUniqueId());
        broadcastState(new PlayerState(true, true, ((CraftPlayer) player).getProfile())); //TODO maybe remove
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
