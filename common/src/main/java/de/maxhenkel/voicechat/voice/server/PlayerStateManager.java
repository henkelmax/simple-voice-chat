package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.net.RemovePlayerStatePacket;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {

    private final ConcurrentHashMap<UUID, PlayerState> states;
    private final Server voicechatServer;

    public PlayerStateManager(Server voicechatServer) {
        this.voicechatServer = voicechatServer;
        this.states = new ConcurrentHashMap<>();

        CommonCompatibilityManager.INSTANCE.getNetManager().updateStateChannel.setServerListener((server, player, handler, packet) -> {
            PlayerState state = states.get(player.getUniqueID());

            if (state == null) {
                state = defaultDisconnectedState(player);
            }

            state.setDisabled(packet.isDisabled());

            states.put(player.getUniqueID(), state);

            broadcastState(player, state);
            Voicechat.LOGGER.debug("Got state of {}: {}", player.getName(), state);
        });
    }

    public void broadcastState(@Nullable EntityPlayerMP stateOwner, PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        for (EntityPlayerMP receiver : voicechatServer.getServer().getPlayerList().getPlayers()) {
            if (stateOwner != null && !CommonCompatibilityManager.INSTANCE.canSee(receiver, stateOwner)) {
                continue;
            }
            NetManager.sendToClient(receiver, packet);
        }
        PluginManager.instance().onPlayerStateChanged(state);
    }

    public void broadcastRemoveState(EntityPlayerMP stateOwner) {
        RemovePlayerStatePacket packet = new RemovePlayerStatePacket(stateOwner.getUniqueID());
        for (EntityPlayerMP receiver : voicechatServer.getServer().getPlayerList().getPlayers()) {
            NetManager.sendToClient(receiver, packet);
        }
        // Send the default disconnected state to the API when disconnecting
        PluginManager.instance().onPlayerStateChanged(defaultDisconnectedState(stateOwner));
    }

    public void onPlayerCompatibilityCheckSucceeded(EntityPlayerMP player) {
        List<PlayerState> stateList = new ArrayList<>(states.size());
        for (PlayerState state : states.values()) {
            EntityPlayerMP otherPlayer = voicechatServer.getServer().getPlayerList().getPlayerByUUID(state.getUuid());
            if (otherPlayer == null) {
                continue;
            }
            if (!CommonCompatibilityManager.INSTANCE.canSee(player, otherPlayer)) {
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
        states.put(player.getUniqueID(), state);
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Setting default state of {}: {}", player.getName(), state);
    }

    public void onPlayerLoggedOut(EntityPlayerMP player) {
        states.remove(player.getUniqueID());
        broadcastRemoveState(player);
        Voicechat.LOGGER.debug("Removing state of {}", player.getGameProfile().getName());
    }

    public void onPlayerHide(EntityPlayerMP visibilityChangedPlayer, EntityPlayerMP observingPlayer) {
        RemovePlayerStatePacket packet = new RemovePlayerStatePacket(visibilityChangedPlayer.getUniqueID());
        NetManager.sendToClient(observingPlayer, packet);
        Voicechat.LOGGER.debug("Removing state of {} for {}", visibilityChangedPlayer.getGameProfile().getName(), observingPlayer.getGameProfile().getName());
    }

    public void onPlayerShow(EntityPlayerMP visibilityChangedPlayer, EntityPlayerMP observingPlayer) {
        PlayerState state = states.get(visibilityChangedPlayer.getUniqueID());
        if (state == null) {
            state = defaultDisconnectedState(visibilityChangedPlayer);
        }
        PlayerStatePacket packet = new PlayerStatePacket(state);
        NetManager.sendToClient(observingPlayer, packet);
        Voicechat.LOGGER.debug("Sending state of {} to {}", visibilityChangedPlayer.getGameProfile().getName(), observingPlayer.getGameProfile().getName());
    }

    public void onPlayerVoicechatDisconnect(UUID uuid) {
        PlayerState state = states.get(uuid);
        if (state == null) {
            return;
        }

        state.setDisconnected(true);

        @Nullable EntityPlayerMP player = voicechatServer.getServer().getPlayerList().getPlayerByUUID(uuid);

        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to disconnected: {}", uuid, state);
    }

    public void onPlayerVoicechatConnect(EntityPlayerMP player) {
        PlayerState state = states.get(player.getUniqueID());

        if (state == null) {
            state = defaultDisconnectedState(player);
        }

        state.setDisconnected(false);

        states.put(player.getUniqueID(), state);

        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to connected: {}", player.getName(), state);
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(EntityPlayerMP player) {
        return new PlayerState(player.getUniqueID(), player.getGameProfile().getName(), false, true);
    }

    public void setGroup(EntityPlayerMP player, @Nullable UUID group) {
        PlayerState state = states.get(player.getUniqueID());
        if (state == null) {
            state = PlayerStateManager.defaultDisconnectedState(player);
            Voicechat.LOGGER.debug("Defaulting to default state for {}: {}", player.getName(), state);
        }
        state.setGroup(group);
        states.put(player.getUniqueID(), state);
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Setting group of {}: {}", player.getName(), state);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
