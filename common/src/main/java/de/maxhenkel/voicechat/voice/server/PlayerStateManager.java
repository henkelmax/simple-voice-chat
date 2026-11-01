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
import net.minecraft.entity.player.EntityPlayerMP;

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

        CommonCompatibilityManager.INSTANCE.getNetManager().updateStateChannel.setServerListener((server, player, handler, packet) -> {
            PlayerState state;
            synchronized (this) {
                state = states.get(player.getUniqueID());
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
        });
    }

    public void broadcastState(@Nullable EntityPlayerMP stateOwner, PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        synchronized (this) {
            // Outdated states of players that already logged out must not be sent anymore
            if (states.get(state.getUuid()) != state) {
                return;
            }
            for (EntityPlayerMP receiver : voicechatServer.getServer().getPlayerList().getPlayers()) {
                if (stateOwner != null && !canSee(receiver, stateOwner)) {
                    continue;
                }
                NetManager.sendToClient(receiver, packet);
            }
        }
        PluginManager.instance().onPlayerStateChanged(state);
    }

    private void broadcastRemoveState(EntityPlayerMP stateOwner) {
        RemovePlayerStatePacket packet = new RemovePlayerStatePacket(stateOwner.getUniqueID());
        for (EntityPlayerMP receiver : voicechatServer.getServer().getPlayerList().getPlayers()) {
            NetManager.sendToClient(receiver, packet);
        }
        // Send the default disconnected state to the API when disconnecting
        PluginManager.instance().onPlayerStateChanged(defaultDisconnectedState(stateOwner));
    }

    public synchronized void onPlayerCompatibilityCheckSucceeded(EntityPlayerMP player) {
        List<PlayerState> stateList = new ArrayList<>(states.size());
        for (PlayerState state : states.values()) {
            EntityPlayerMP otherPlayer = voicechatServer.getServer().getPlayerList().getPlayerByUUID(state.getUuid());
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

    public void onPlayerLoggedIn(EntityPlayerMP player) {
        PlayerState state = defaultDisconnectedState(player);
        synchronized (this) {
            states.put(player.getUniqueID(), state);
            for (EntityPlayerMP observer : voicechatServer.getServer().getPlayerList().getPlayers()) {
                updateVisibility(observer, player);
            }
        }
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Setting default state of {}: {}", player.getName(), state);
        if (AudioSenderImpl.getRegisteredPlayers().contains(player.getUniqueID())) {
            evaluateForcedState(player);
        }
    }

    public void onPlayerLoggedOut(EntityPlayerMP player) {
        synchronized (this) {
            states.remove(player.getUniqueID());
            forgetVisibility(player);
        }
        broadcastRemoveState(player);
        Voicechat.LOGGER.debug("Removing state of {}", player.getGameProfile().getName());
    }

    public synchronized void onPlayerHide(EntityPlayerMP visibilityChangedPlayer, EntityPlayerMP observingPlayer) {
        setHidden(observingPlayer, visibilityChangedPlayer, true);
        RemovePlayerStatePacket packet = new RemovePlayerStatePacket(visibilityChangedPlayer.getUniqueID());
        NetManager.sendToClient(observingPlayer, packet);
        Voicechat.LOGGER.debug("Removing state of {} for {}", visibilityChangedPlayer.getGameProfile().getName(), observingPlayer.getGameProfile().getName());
    }

    public synchronized void onPlayerShow(EntityPlayerMP visibilityChangedPlayer, EntityPlayerMP observingPlayer) {
        if (!updateVisibility(observingPlayer, visibilityChangedPlayer, true)) {
            return;
        }
        PlayerState state = states.get(visibilityChangedPlayer.getUniqueID());
        if (state == null) {
            state = defaultDisconnectedState(visibilityChangedPlayer);
        }
        PlayerStatePacket packet = new PlayerStatePacket(state);
        NetManager.sendToClient(observingPlayer, packet);
        Voicechat.LOGGER.debug("Sending state of {} to {}", visibilityChangedPlayer.getGameProfile().getName(), observingPlayer.getGameProfile().getName());
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

        @Nullable EntityPlayerMP player = voicechatServer.getServer().getPlayerList().getPlayerByUUID(uuid);

        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to disconnected: {}", uuid, state);
    }

    public void onPlayerVoicechatConnect(EntityPlayerMP player) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUniqueID());
            if (state == null) {
                return;
            }
            state.setDisconnected(false);
        }
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to connected: {}", player.getName(), state);
    }

    public void evaluateForcedStateIfPermissionsChanged(EntityPlayerMP player) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUniqueID());
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

    public void evaluateForcedState(EntityPlayerMP player) {
        PlayerState state;
        synchronized (this) {
            checkIfStateCanBeModified();
            state = states.get(player.getUniqueID());
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
    private boolean evaluateForcedState(EntityPlayerMP player, PlayerState state, boolean speakPermission, boolean listenPermission) {
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

    /**
     * @throws IllegalStateException if called from within a {@link PlayerForcedStateEvent} or a {@link PlayerVisibilityEvent}
     */
    public synchronized void checkIfStateCanBeModified() {
        if (evaluatingForcedState || evaluatingVisibility) {
            throw new IllegalStateException("Can't modify the state of a player from within an event that modifies the state of a player itself");
        }
    }

    public synchronized void evaluateVisibility(EntityPlayerMP player) {
        checkIfStateCanBeModified();
        PlayerState state = states.get(player.getUniqueID());
        if (state == null) {
            return;
        }
        PlayerStatePacket statePacket = new PlayerStatePacket(state);
        RemovePlayerStatePacket removePacket = new RemovePlayerStatePacket(player.getUniqueID());
        for (EntityPlayerMP observer : voicechatServer.getServer().getPlayerList().getPlayers()) {
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
    private boolean updateVisibility(EntityPlayerMP observer, EntityPlayerMP player) {
        return updateVisibility(observer, player, CommonCompatibilityManager.INSTANCE.canSee(observer, player));
    }

    private synchronized boolean updateVisibility(EntityPlayerMP observer, EntityPlayerMP player, boolean visibleOnPlatform) {
        boolean visible = emitVisibilityEvent(observer, player, visibleOnPlatform);
        setHidden(observer, player, !visible);
        return visible;
    }

    private boolean emitVisibilityEvent(EntityPlayerMP observer, EntityPlayerMP player, boolean visibleOnPlatform) {
        if (observer.getUniqueID().equals(player.getUniqueID())) {
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
    private boolean canSee(EntityPlayerMP observer, EntityPlayerMP player) {
        return CommonCompatibilityManager.INSTANCE.canSee(observer, player) && !isHidden(observer, player);
    }

    private boolean isHidden(EntityPlayerMP observer, EntityPlayerMP player) {
        Set<UUID> hiddenPlayers = hiddenPlayersByObserver.get(observer.getUniqueID());
        return hiddenPlayers != null && hiddenPlayers.contains(player.getUniqueID());
    }

    private synchronized void setHidden(EntityPlayerMP observer, EntityPlayerMP player, boolean hidden) {
        // Observers that already logged out must not get new entries
        if (!states.containsKey(observer.getUniqueID())) {
            return;
        }
        if (hidden) {
            hiddenPlayersByObserver.computeIfAbsent(observer.getUniqueID(), uuid -> ConcurrentHashMap.newKeySet()).add(player.getUniqueID());
            return;
        }
        Set<UUID> hiddenPlayers = hiddenPlayersByObserver.get(observer.getUniqueID());
        if (hiddenPlayers != null) {
            hiddenPlayers.remove(player.getUniqueID());
        }
    }

    private void forgetVisibility(EntityPlayerMP player) {
        hiddenPlayersByObserver.remove(player.getUniqueID());
        hiddenPlayersByObserver.values().forEach(hiddenPlayers -> hiddenPlayers.remove(player.getUniqueID()));
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(EntityPlayerMP player) {
        return new PlayerState(player.getUniqueID(), player.getGameProfile().getName(), false, false, true);
    }

    public void setGroup(EntityPlayerMP player, @Nullable UUID group) {
        PlayerState state;
        synchronized (this) {
            state = states.get(player.getUniqueID());
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
