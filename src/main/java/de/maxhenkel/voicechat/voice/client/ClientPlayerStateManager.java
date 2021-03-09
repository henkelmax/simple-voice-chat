package de.maxhenkel.voicechat.voice.client;


import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.net.PlayerStateMessage;
import de.maxhenkel.voicechat.net.PlayerStatesMessage;
import de.maxhenkel.voicechat.net.SetPlayerStateMessage;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientPlayerStateManager {

    private boolean muted;
    private PlayerState state;
    private Map<UUID, PlayerState> states;

    public ClientPlayerStateManager() {
        muted = Main.CLIENT_CONFIG.muted.get();
        state = new PlayerState(Main.CLIENT_CONFIG.disabled.get(), true);
        states = new HashMap<>();
    }

    public void onPlayerStatePacket(PlayerStateMessage packet) {
        states.put(packet.getUuid(), packet.getPlayerState());
    }

    public void onPlayerStatesPacket(PlayerStatesMessage packet) {
        states = packet.getPlayerStates();
    }

    /**
     * Called when the voicechat client gets disconnected or the player logs out
     */
    public void onVoiceChatDisconnected() {
        state.setDisconnected(true);
        syncOwnState();
    }

    /**
     * Called when the voicechat client gets (re)connected
     */
    public void onVoiceChatConnected(Client client) {
        state.setDisconnected(false);
        syncOwnState();
    }

    public void onDisconnect() {
        clearStates();
    }

    public boolean isPlayerDisabled(PlayerEntity player) {
        PlayerState playerState = states.get(player.getUniqueID());
        if (playerState == null) {
            return false;
        }

        return playerState.isDisabled();
    }

    public boolean isPlayerDisconnected(PlayerEntity player) {
        PlayerState playerState = states.get(player.getUniqueID());
        if (playerState == null) {
            return true;
        }

        return playerState.isDisconnected();
    }

    public void syncOwnState() {
        Main.SIMPLE_CHANNEL.sendToServer(new SetPlayerStateMessage(state));
    }

    public boolean isDisabled() {
        return state.isDisabled();
    }

    public boolean isDisconnected() {
        return state.isDisconnected();
    }

    public void setDisabled(boolean disabled) {
        state.setDisabled(disabled);
        Main.CLIENT_CONFIG.disabled.set(disabled);
        Main.CLIENT_CONFIG.disabled.save();
        syncOwnState();
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        Main.CLIENT_CONFIG.muted.set(muted);
        Main.CLIENT_CONFIG.muted.save();
    }

    public void clearStates() {
        states.clear();
    }
}
