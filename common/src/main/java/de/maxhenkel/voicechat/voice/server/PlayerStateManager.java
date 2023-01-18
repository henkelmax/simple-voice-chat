package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.entity.player.EntityPlayerMP;

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
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedIn(this::onPlayerLoggedIn);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(this::onPlayerLoggedOut);
        CommonCompatibilityManager.INSTANCE.onServerVoiceChatConnected(this::onPlayerVoicechatConnect);
        CommonCompatibilityManager.INSTANCE.onServerVoiceChatDisconnected(this::onPlayerVoicechatDisconnect);
        CommonCompatibilityManager.INSTANCE.onPlayerCompatibilityCheckSucceeded(this::onPlayerCompatibilityCheckSucceeded);

        CommonCompatibilityManager.INSTANCE.getNetManager().updateStateChannel.setServerListener((server, player, handler, packet) -> {
            PlayerState state = states.get(player.getUniqueID());

            if (state == null) {
                state = defaultDisconnectedState(player);
            }

            state.setDisabled(packet.isDisabled());

            states.put(player.getUniqueID(), state);

            broadcastState(state);
            Voicechat.logDebug("Got state of {}: {}", player.getDisplayNameString(), state);
        });
    }

    private void broadcastState(PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        voicechatServer.getServer().getPlayerList().getPlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    private void onPlayerCompatibilityCheckSucceeded(EntityPlayerMP player) {
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        NetManager.sendToClient(player, packet);
        Voicechat.logDebug("Sending initial states to {}", player.getDisplayNameString());
    }

    private void onPlayerLoggedIn(EntityPlayerMP player) {
        PlayerState state = defaultDisconnectedState(player);
        states.put(player.getUniqueID(), state);
        broadcastState(state);
        Voicechat.logDebug("Setting default state of {}: {}", player.getDisplayNameString(), state);
    }

    private void onPlayerLoggedOut(EntityPlayerMP player) {
        states.remove(player.getUniqueID());
        broadcastState(new PlayerState(player.getUniqueID(), player.getGameProfile().getName(), false, true));
        Voicechat.logDebug("Removing state of {}", player.getDisplayNameString());
    }

    private void onPlayerVoicechatDisconnect(UUID uuid) {
        PlayerState state = states.get(uuid);
        if (state == null) {
            return;
        }

        state.setDisconnected(true);

        broadcastState(state);
        Voicechat.logDebug("Set state of {} to disconnected: {}", uuid, state);
    }

    private void onPlayerVoicechatConnect(EntityPlayerMP player) {
        PlayerState state = states.get(player.getUniqueID());

        if (state == null) {
            state = defaultDisconnectedState(player);
        }

        state.setDisconnected(false);

        states.put(player.getUniqueID(), state);

        broadcastState(state);
        Voicechat.logDebug("Set state of {} to connected: {}", player.getDisplayNameString(), state);
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(EntityPlayerMP player) {
        return new PlayerState(player.getUniqueID(), player.getGameProfile().getName(), false, true);
    }

    public void setGroup(EntityPlayerMP player, @Nullable ClientGroup group) {
        PlayerState state = states.get(player.getUniqueID());
        if (state == null) {
            state = PlayerStateManager.defaultDisconnectedState(player);
            Voicechat.logDebug("Defaulting to default state for {}: {}", player.getDisplayNameString(), state);
        }
        state.setGroup(group);
        states.put(player.getUniqueID(), state);
        broadcastState(state);
        Voicechat.logDebug("Setting group of {}: {}", player.getDisplayNameString(), state);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
