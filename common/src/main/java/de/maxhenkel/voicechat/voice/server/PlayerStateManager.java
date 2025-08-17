package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {

    private final ConcurrentHashMap<UUID, PlayerState> states;
    private final Server voicechatServer;

    public PlayerStateManager(Server voicechatServer) {
        this.voicechatServer = voicechatServer;
        this.states = new ConcurrentHashMap<>();

        CommonCompatibilityManager.INSTANCE.getNetManager().updateStateChannel.setServerListener((server, player, handler, packet) -> {
            PlayerState state = states.get(player.getUUID());

            if (state == null) {
                state = defaultDisconnectedState(player);
            }

            state.setDisabled(packet.isDisabled());

            states.put(player.getUUID(), state);

            broadcastState(player, state);
            Voicechat.LOGGER.debug("Got state of {}: {}", player.getName().getString(), state);
        });
    }

    public void broadcastState(@Nullable ServerPlayer stateOwner, PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        for (ServerPlayer receiver : voicechatServer.getServer().getPlayerList().getPlayers()) {
            if (stateOwner != null && !CommonCompatibilityManager.INSTANCE.canSee(receiver, stateOwner)) {
                continue;
            }
            NetManager.sendToClient(receiver, packet);
        }
        PluginManager.instance().onPlayerStateChanged(state);
    }

    public void onPlayerCompatibilityCheckSucceeded(ServerPlayerEntity player) {
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        NetManager.sendToClient(player, packet);
        Voicechat.LOGGER.debug("Sending initial states to {}", player.getName().getString());
    }

    public void onPlayerLoggedIn(ServerPlayerEntity player) {
        PlayerState state = defaultDisconnectedState(player);
        states.put(player.getUUID(), state);
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Setting default state of {}: {}", player.getName().getString(), state);
    }

    public void onPlayerLoggedOut(ServerPlayerEntity player) {
        states.remove(player.getUUID());
        broadcastState(player, new PlayerState(player.getUUID(), player.getGameProfile().getName(), false, true));
        Voicechat.LOGGER.debug("Removing state of {}", player.getName().getString());
    }

    public void onPlayerHide(CommonCompatibilityManager.PlayerVisibilityEvent event) {
        PlayerStatePacket packet = new PlayerStatePacket(defaultDisconnectedState(event.getVisibilityChangedPlayer()));
        NetManager.sendToClient(event.getObservingPlayer(), packet);
        Voicechat.LOGGER.debug("Sending hidden state of {} to {}", event.getVisibilityChangedPlayer().getName().getString(), event.getObservingPlayer().getName().getString());
    }

    public void onPlayerShow(CommonCompatibilityManager.PlayerVisibilityEvent event) {
        PlayerState state = states.get(event.getVisibilityChangedPlayer().getUUID());
        if (state == null) {
            state = defaultDisconnectedState(event.getObservingPlayer());
        }
        PlayerStatePacket packet = new PlayerStatePacket(state);
        NetManager.sendToClient(event.getObservingPlayer(), packet);
        Voicechat.LOGGER.debug("Sending shown state of {} to {}", event.getVisibilityChangedPlayer().getName().getString(), event.getObservingPlayer().getName().getString());
    }

    public void onPlayerVoicechatDisconnect(UUID uuid) {
        PlayerState state = states.get(uuid);
        if (state == null) {
            return;
        }

        state.setDisconnected(true);

        @Nullable ServerPlayer player = voicechatServer.getServer().getPlayerList().getPlayer(uuid);

        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to disconnected: {}", uuid, state);
    }

    public void onPlayerVoicechatConnect(ServerPlayerEntity player) {
        PlayerState state = states.get(player.getUUID());

        if (state == null) {
            state = defaultDisconnectedState(player);
        }

        state.setDisconnected(false);

        states.put(player.getUUID(), state);

        broadcastState(player, state);
        Voicechat.LOGGER.debug("Set state of {} to connected: {}", player.getName().getString(), state);
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(ServerPlayerEntity player) {
        return new PlayerState(player.getUUID(), player.getGameProfile().getName(), false, true);
    }

    public void setGroup(ServerPlayerEntity player, @Nullable UUID group) {
        PlayerState state = states.get(player.getUUID());
        if (state == null) {
            state = PlayerStateManager.defaultDisconnectedState(player);
            Voicechat.LOGGER.debug("Defaulting to default state for {}: {}", player.getName().getString(), state);
        }
        state.setGroup(group);
        states.put(player.getUUID(), state);
        broadcastState(player, state);
        Voicechat.LOGGER.debug("Setting group of {}: {}", player.getName().getString(), state);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
