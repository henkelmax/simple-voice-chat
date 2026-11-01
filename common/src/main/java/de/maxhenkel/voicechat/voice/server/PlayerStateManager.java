package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.events.PlayerForcedStateEvent;
import de.maxhenkel.voicechat.api.events.PlayerVisibilityEvent;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.net.RemovePlayerStatePacket;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.audiosender.AudioSenderImpl;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {

    private final ConcurrentHashMap<UUID, PlayerState> states;
    private final ConcurrentHashMap<UUID, Set<UUID>> hiddenPlayersByObserver;
    private boolean evaluatingForcedState;
    private boolean evaluatingVisibility;
    private final Server voicechatServer;

    public PlayerStateManager(Server voicechatServer) {
        this.voicechatServer = voicechatServer;
        this.states = new ConcurrentHashMap<>();
        this.hiddenPlayersByObserver = new ConcurrentHashMap<>();

        CommonCompatibilityManager.INSTANCE.getNetManager().updateStateChannel.setServerListener((player, packet) -> {
            PlayerState state;
            synchronized (this) {
                state = states.get(player.getUUID());
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
            Voicechat.LOGGER.debug("Got state of {}: {}", player.getName().getString(), state);
        });
    }

    public void broadcastState(@Nullable ServerPlayer stateOwner, PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        synchronized (this) {
            // Outdated states of players that already logged out must not be sent anymore
            if (states.get(state.getUuid()) != state) {
                return;
            }
            for (ServerPlayer receiver : voicechatServer.getServer().getPlayerList().getPlayers()) {
                if (stateOwner != null && !canSee(receiver, stateOwner)) {
                    continue;
                }
                NetManager.sendToClient(receiver, packet);
            }
        }
        PluginManager.instance().onPlayerStateChanged(state);
    }

    private void broadcastRemoveState(ServerPlayer stateOwner) {
        RemovePlayerStatePacket packet = new RemovePlayerStatePacket(stateOwner.getUUID());
        for (ServerPlayer receiver : voicechatServer.getServer().getPlayerList().getPlayers()) {
            NetManager.sendToClient(receiver, packet);
        }
        // Send the default disconnected state to the API when disconnecting
        PluginManager.instance().onPlayerStateChanged(defaultDisconnectedState(stateOwner));
    }

    public synchronized void onPlayerCompatibilityCheckSucceeded(ServerPlayer player) {
        List<PlayerState> stateList = new ArrayList<>(states.size());
        for (PlayerState state : states.values()) {
            ServerPlayer otherPlayer = voicechatServer.getServer().getPlayerList().getPlayer(state.getUuid());
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
        Voicechat.LOGGER.debug("Sending initial states to {}", player.getName().getString());
    }

    public void onPlayerLoggedIn(ServerPlayer player) {
        PlayerState state = defaultDisconnectedState(player);
        synchronized (this) {
            states.put(player.getUUID(), state);
            for (ServerPlayer observer : voicechatServer.getServer().getPlayerList().getPlayers()) {
                updateVisibility(observer, player);
            }
        }
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Setting default state of {}: {}", player.getName().getString(), state);
        if (AudioSenderImpl.getRegisteredPlayers().contains(player.getUUID())) {
            evaluateForcedState(player);
        }
    }

    public void onPlayerLoggedOut(ServerPlayer player) {
        synchronized (this) {
            states.remove(player.getUUID());
            forgetVisibility(player);
        }
        broadcastRemoveState(player);
        Voicechat.LOGGER.debug("Removing state of {}", player.getName().getString());
    }

    public synchronized void onPlayerHide(ServerPlayer visibilityChangedPlayer, ServerPlayer observingPlayer) {
        setHidden(observingPlayer, visibilityChangedPlayer, true);
        RemovePlayerStatePacket packet = new RemovePlayerStatePacket(visibilityChangedPlayer.getUUID());
        NetManager.sendToClient(observingPlayer, packet);
        Voicechat.LOGGER.debug("Removing state of {} for {}", visibilityChangedPlayer.getName().getString(), observingPlayer.getName().getString());
    }

    public synchronized void onPlayerShow(ServerPlayer visibilityChangedPlayer, ServerPlayer observingPlayer) {
        if (!updateVisibility(observingPlayer, visibilityChangedPlayer, true)) {
            return;
        }
        PlayerState state = states.get(visibilityChangedPlayer.getUUID());
        if (state == null) {
            state = defaultDisconnectedState(visibilityChangedPlayer);
        }
        PlayerStatePacket packet = new PlayerStatePacket(state);
        NetManager.sendToClient(observingPlayer, packet);
        Voicechat.LOGGER.debug("Sending state of {} to {}", visibilityChangedPlayer.getName().getString(), observingPlayer.getName().getString());
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

        @Nullable ServerPlayer player = voicechatServer.getServer().getPlayerList().getPlayer(uuid);

        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to disconnected: {}", uuid, state);
    }

    public void onPlayerVoicechatConnect(ServerPlayer player) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUUID());
            if (state == null) {
                return;
            }
            state.setDisconnected(false);
        }
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to connected: {}", player.getName().getString(), state);
    }

    public void evaluateForcedStateIfPermissionsChanged(ServerPlayer player) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUUID());
            if (state == null) {
                return;
            }
            boolean speakPermission = PermissionManager.INSTANCE.SPEAK_PERMISSION.hasPermission(player);
            boolean listenPermission = PermissionManager.INSTANCE.LISTEN_PERMISSION.hasPermission(player);
            if (state.hasSpeakPermission() == speakPermission && state.hasListenPermission() == listenPermission) {
                return;
            }
            if (!evaluateForcedState(player, state, speakPermission, listenPermission)) {
                return;
            }
        }
        broadcastState(player, state);
    }

    public void evaluateForcedState(ServerPlayer player) {
        PlayerState state;
        synchronized (this) {
            checkIfStateCanBeModified();
            state = states.get(player.getUUID());
            if (state == null) {
                return;
            }
            if (!evaluateForcedState(player, state, PermissionManager.INSTANCE.SPEAK_PERMISSION.hasPermission(player), PermissionManager.INSTANCE.LISTEN_PERMISSION.hasPermission(player))) {
                return;
            }
        }
        broadcastState(player, state);
    }

    /**
     * @return if the forced state changed
     */
    private boolean evaluateForcedState(ServerPlayer player, PlayerState state, boolean speakPermission, boolean listenPermission) {
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
        Voicechat.LOGGER.debug("Updated forced state of {}: {}", player.getName().getString(), state);
        return true;
    }

    /**
     * @throws IllegalStateException if called from within a {@link PlayerForcedStateEvent} or a {@link PlayerVisibilityEvent}
     */
    public synchronized void checkIfStateCanBeModified() {
        if (evaluatingForcedState || evaluatingVisibility) {
            throw new IllegalStateException("Can't modify the state of a player from within an event that modifies the state of a player itself");
        }
    }

    public synchronized void evaluateVisibility(ServerPlayer player) {
        checkIfStateCanBeModified();
        PlayerState state = states.get(player.getUUID());
        if (state == null) {
            return;
        }
        PlayerStatePacket statePacket = new PlayerStatePacket(state);
        RemovePlayerStatePacket removePacket = new RemovePlayerStatePacket(player.getUUID());
        for (ServerPlayer observer : voicechatServer.getServer().getPlayerList().getPlayers()) {
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
        Voicechat.LOGGER.debug("Evaluated visibility of {}", player.getName().getString());
    }

    /**
     * Emits a {@link PlayerVisibilityEvent} and remembers the result.
     *
     * @return if the observer can see the player
     */
    private boolean updateVisibility(ServerPlayer observer, ServerPlayer player) {
        return updateVisibility(observer, player, CommonCompatibilityManager.INSTANCE.canSee(observer, player));
    }

    private synchronized boolean updateVisibility(ServerPlayer observer, ServerPlayer player, boolean visibleOnPlatform) {
        boolean visible = emitVisibilityEvent(observer, player, visibleOnPlatform);
        setHidden(observer, player, !visible);
        return visible;
    }

    private boolean emitVisibilityEvent(ServerPlayer observer, ServerPlayer player, boolean visibleOnPlatform) {
        if (observer.getUUID().equals(player.getUUID())) {
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
    private boolean canSee(ServerPlayer observer, ServerPlayer player) {
        return CommonCompatibilityManager.INSTANCE.canSee(observer, player) && !isHidden(observer, player);
    }

    private boolean isHidden(ServerPlayer observer, ServerPlayer player) {
        Set<UUID> hiddenPlayers = hiddenPlayersByObserver.get(observer.getUUID());
        return hiddenPlayers != null && hiddenPlayers.contains(player.getUUID());
    }

    private synchronized void setHidden(ServerPlayer observer, ServerPlayer player, boolean hidden) {
        // Observers that already logged out must not get new entries
        if (!states.containsKey(observer.getUUID())) {
            return;
        }
        if (hidden) {
            hiddenPlayersByObserver.computeIfAbsent(observer.getUUID(), uuid -> ConcurrentHashMap.newKeySet()).add(player.getUUID());
            return;
        }
        Set<UUID> hiddenPlayers = hiddenPlayersByObserver.get(observer.getUUID());
        if (hiddenPlayers != null) {
            hiddenPlayers.remove(player.getUUID());
        }
    }

    private void forgetVisibility(ServerPlayer player) {
        hiddenPlayersByObserver.remove(player.getUUID());
        hiddenPlayersByObserver.values().forEach(hiddenPlayers -> hiddenPlayers.remove(player.getUUID()));
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(ServerPlayer player) {
        return new PlayerState(player.getUUID(), player.getGameProfile().getName(), false, false, true);
    }

    public void setGroup(ServerPlayer player, @Nullable UUID group) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUUID());
            if (state == null) {
                return;
            }
            state.setGroup(group);
        }
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Setting group of {}: {}", player.getName().getString(), state);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
