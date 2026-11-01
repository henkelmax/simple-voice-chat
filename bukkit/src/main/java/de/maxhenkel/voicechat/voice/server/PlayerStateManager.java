package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.events.PlayerForcedStateEvent;
import de.maxhenkel.voicechat.api.events.PlayerVisibilityEvent;
import de.maxhenkel.voicechat.compatibility.PlayerHideEvent;
import de.maxhenkel.voicechat.compatibility.PlayerShowEvent;
import de.maxhenkel.voicechat.net.*;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.audiosender.AudioSenderImpl;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager implements Listener {

    private final ConcurrentHashMap<UUID, PlayerState> states;
    private final ConcurrentHashMap<UUID, Set<UUID>> hiddenPlayersByObserver;
    private boolean evaluatingForcedState;
    private boolean evaluatingVisibility;

    public PlayerStateManager() {
        this.states = new ConcurrentHashMap<>();
        this.hiddenPlayersByObserver = new ConcurrentHashMap<>();
    }

    public void onUpdateStatePacket(Player player, UpdateStatePacket packet) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUniqueId());
            if (state == null) {
                return;
            }
            if (state.isDisabled() == packet.isDisabled() && state.isMuted() == packet.isMuted()) {
                return;
            }
            state.setDisabled(packet.isDisabled());
            state.setMuted(packet.isMuted());
        }
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Got state of {}: {}", player.getName(), state);
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        synchronized (this) {
            states.remove(event.getPlayer().getUniqueId());
            forgetVisibility(event.getPlayer());
        }
        broadcastRemoveState(event.getPlayer());
        Voicechat.LOGGER.debug("Removing state of {}", event.getPlayer().getName());
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerState state = defaultDisconnectedState(event.getPlayer());
        synchronized (this) {
            states.put(event.getPlayer().getUniqueId(), state);
            for (Player observer : Voicechat.INSTANCE.getServer().getOnlinePlayers()) {
                updateVisibility(observer, event.getPlayer());
            }
        }
        broadcastState(event.getPlayer(), state);
        Voicechat.LOGGER.debug("Setting default state of {}: {}", event.getPlayer().getName(), state);
        if (AudioSenderImpl.getRegisteredPlayers().contains(event.getPlayer().getUniqueId())) {
            evaluateForcedState(event.getPlayer());
        }
    }

    public synchronized void onPlayerHide(PlayerHideEvent event) {
        setHidden(event.getObserver(), event.getHiddenPlayer(), true);
        RemovePlayerStatePacket packet = new RemovePlayerStatePacket(event.getHiddenPlayer().getUniqueId());
        NetManager.sendToClient(event.getObserver(), packet);
        Voicechat.LOGGER.debug("Removing state of {} for {}", event.getHiddenPlayer().getName(), event.getObserver().getName());
    }

    public synchronized void onPlayerShow(PlayerShowEvent event) {
        if (!updateVisibility(event.getObserver(), event.getShownPlayer(), true)) {
            return;
        }
        PlayerState state = states.get(event.getShownPlayer().getUniqueId());
        if (state == null) {
            state = defaultDisconnectedState(event.getShownPlayer());
        }
        PlayerStatePacket packet = new PlayerStatePacket(state);
        NetManager.sendToClient(event.getObserver(), packet);
        Voicechat.LOGGER.debug("Sending state of {} to {}", event.getShownPlayer().getName(), event.getObserver().getName());
    }

    public void broadcastState(@Nullable Player stateOwner, PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        synchronized (this) {
            // Outdated states of players that already logged out must not be sent anymore
            if (states.get(state.getUuid()) != state) {
                return;
            }
            for (Player receiver : Voicechat.INSTANCE.getServer().getOnlinePlayers()) {
                if (stateOwner != null && !canSee(receiver, stateOwner)) {
                    continue;
                }
                NetManager.sendToClient(receiver, packet);
            }
        }
        PluginManager.instance().onPlayerStateChanged(state);
    }

    private void broadcastRemoveState(Player stateOwner) {
        RemovePlayerStatePacket packet = new RemovePlayerStatePacket(stateOwner.getUniqueId());
        for (Player receiver : Voicechat.INSTANCE.getServer().getOnlinePlayers()) {
            NetManager.sendToClient(receiver, packet);
        }
        // Send the default disconnected state to the API when disconnecting
        PluginManager.instance().onPlayerStateChanged(defaultDisconnectedState(stateOwner));
    }

    public synchronized void onPlayerCompatibilityCheckSucceeded(Player player) {
        List<PlayerState> stateList = new ArrayList<>(states.size());
        for (PlayerState state : states.values()) {
            Player otherPlayer = Voicechat.INSTANCE.getServer().getPlayer(state.getUuid());
            if (otherPlayer == null) {
                continue;
            }
            if (!updateVisibility(player, otherPlayer)) {
                continue;
            }
            stateList.add(state);
        }
        PlayerStatesPacket packet = new PlayerStatesPacket(stateList);
        NetManager.sendToClient(player, packet);
        Voicechat.LOGGER.debug("Sending initial states to {}", player.getName());
    }

    public void onPlayerVoicechatDisconnect(UUID uuid) {
        PlayerState state;
        synchronized (this) {
            state = states.get(uuid);
            if (state == null) {
                return;
            }
            state.setDisconnected(true);
        }

        @Nullable Player player = Bukkit.getPlayer(uuid);

        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to disconnected: {}", uuid, state);
    }

    public void onPlayerVoicechatConnect(Player player) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUniqueId());
            if (state == null) {
                return;
            }
            state.setDisconnected(false);
        }
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to connected: {}", player.getName(), state);
    }

    public void evaluateForcedStateIfPermissionsChanged(Player player) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUniqueId());
            if (state == null) {
                return;
            }
            boolean speakPermission = player.hasPermission(PermissionManager.SPEAK_PERMISSION);
            boolean listenPermission = player.hasPermission(PermissionManager.LISTEN_PERMISSION);
            if (state.hasSpeakPermission() == speakPermission && state.hasListenPermission() == listenPermission) {
                return;
            }
            if (!evaluateForcedState(player, state, speakPermission, listenPermission)) {
                return;
            }
        }
        broadcastState(player, state);
    }

    public void evaluateForcedState(Player player) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUniqueId());
            if (state == null) {
                return;
            }
            if (!evaluateForcedState(player, state, player.hasPermission(PermissionManager.SPEAK_PERMISSION), player.hasPermission(PermissionManager.LISTEN_PERMISSION))) {
                return;
            }
        }
        broadcastState(player, state);
    }

    /**
     * @return if the forced state changed
     */
    private boolean evaluateForcedState(Player player, PlayerState state, boolean speakPermission, boolean listenPermission) {
        if (evaluatingForcedState) {
            throw new IllegalStateException("Can't evaluate the forced state from within a PlayerForcedStateEvent");
        }
        state.setSpeakPermission(speakPermission);
        state.setListenPermission(listenPermission);
        PlayerForcedStateEvent event;
        evaluatingForcedState = true;
        try {
            event = PluginManager.instance().onPlayerForcedState(player, state, !speakPermission, !listenPermission);
        } finally {
            evaluatingForcedState = false;
        }
        if (state.isForceMuted() == event.isForceMuted() && state.isForceDisabled() == event.isForceDisabled()) {
            return false;
        }
        state.setForceMuted(event.isForceMuted());
        state.setForceDisabled(event.isForceDisabled());
        Voicechat.LOGGER.debug("Updated forced state of {}: {}", player.getName(), state);
        return true;
    }

    public synchronized void evaluateVisibility(Player player) {
        if (evaluatingVisibility) {
            throw new IllegalStateException("Can't evaluate the visibility from within a PlayerVisibilityEvent");
        }
        PlayerState state = states.get(player.getUniqueId());
        if (state == null) {
            return;
        }
        PlayerStatePacket statePacket = new PlayerStatePacket(state);
        RemovePlayerStatePacket removePacket = new RemovePlayerStatePacket(player.getUniqueId());
        for (Player observer : Voicechat.INSTANCE.getServer().getOnlinePlayers()) {
            boolean wasVisible = !isHidden(observer, player);
            boolean visible = updateVisibility(observer, player);
            if (visible == wasVisible) {
                continue;
            }
            if (visible) {
                NetManager.sendToClient(observer, statePacket);
            } else {
                NetManager.sendToClient(observer, removePacket);
            }
        }
        Voicechat.LOGGER.debug("Evaluated visibility of {}", player.getName());
    }

    /**
     * Emits a {@link PlayerVisibilityEvent} and remembers the result.
     *
     * @return if the observer can see the player
     */
    private boolean updateVisibility(Player observer, Player player) {
        return updateVisibility(observer, player, Voicechat.compatibility.canSee(observer, player));
    }

    private synchronized boolean updateVisibility(Player observer, Player player, boolean visibleOnPlatform) {
        boolean visible = emitVisibilityEvent(observer, player, visibleOnPlatform);
        setHidden(observer, player, !visible);
        return visible;
    }

    private boolean emitVisibilityEvent(Player observer, Player player, boolean visibleOnPlatform) {
        if (observer.getUniqueId().equals(player.getUniqueId())) {
            return true;
        }
        evaluatingVisibility = true;
        try {
            return !PluginManager.instance().isHidden(observer, player, !visibleOnPlatform);
        } finally {
            evaluatingVisibility = false;
        }
    }

    /**
     * Uses the remembered visibility without emitting a {@link PlayerVisibilityEvent}.
     *
     * @return if the observer can see the player
     */
    private boolean canSee(Player observer, Player player) {
        return Voicechat.compatibility.canSee(observer, player) && !isHidden(observer, player);
    }

    private boolean isHidden(Player observer, Player player) {
        Set<UUID> hiddenPlayers = hiddenPlayersByObserver.get(observer.getUniqueId());
        return hiddenPlayers != null && hiddenPlayers.contains(player.getUniqueId());
    }

    private synchronized void setHidden(Player observer, Player player, boolean hidden) {
        // Observers that already logged out must not get new entries
        if (!states.containsKey(observer.getUniqueId())) {
            return;
        }
        if (hidden) {
            hiddenPlayersByObserver.computeIfAbsent(observer.getUniqueId(), uuid -> ConcurrentHashMap.newKeySet()).add(player.getUniqueId());
            return;
        }
        Set<UUID> hiddenPlayers = hiddenPlayersByObserver.get(observer.getUniqueId());
        if (hiddenPlayers != null) {
            hiddenPlayers.remove(player.getUniqueId());
        }
    }

    private void forgetVisibility(Player player) {
        hiddenPlayersByObserver.remove(player.getUniqueId());
        hiddenPlayersByObserver.values().forEach(hiddenPlayers -> hiddenPlayers.remove(player.getUniqueId()));
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(Player player) {
        return new PlayerState(player.getUniqueId(), player.getName(), false, false, true);
    }

    public void setGroup(Player player, @Nullable UUID group) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUniqueId());
            if (state == null) {
                return;
            }
            state.setGroup(group);
        }
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Setting group of {}: {}", player.getName(), state);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
