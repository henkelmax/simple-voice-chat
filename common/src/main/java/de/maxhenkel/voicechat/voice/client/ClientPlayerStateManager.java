package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

public class ClientPlayerStateManager {

    private boolean muted;
    private PlayerState state;
    private Map<UUID, PlayerState> states;

    public ClientPlayerStateManager() {
        muted = VoicechatClient.CLIENT_CONFIG.muted.get();
        state = getDefaultState();
        states = new HashMap<>();

        CommonCompatibilityManager.INSTANCE.getNetManager().playerStateChannel.registerServerListener((client, handler, packet) -> {
            states.put(packet.getPlayerState().getGameProfile().getId(), packet.getPlayerState());
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().playerStatesChannel.registerServerListener((client, handler, packet) -> {
            states = packet.getPlayerStates();
        });
        ClientCompatibilityManager.INSTANCE.onVoiceChatConnected(this::onVoiceChatConnected);
        ClientCompatibilityManager.INSTANCE.onVoiceChatDisconnected(this::onVoiceChatDisconnected);
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::onDisconnect);
    }

    private PlayerState getDefaultState() {
        return new PlayerState(VoicechatClient.CLIENT_CONFIG.disabled.get(), true, Minecraft.getInstance().getUser().getGameProfile());
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
    public void onVoiceChatConnected(ClientVoicechatConnection client) {
        state.setDisconnected(false);
        syncOwnState();
    }

    private void onDisconnect() {
        clearStates();
        state = getDefaultState();
    }

    public boolean isPlayerDisabled(Player player) {
        PlayerState playerState = states.get(player.getUUID());
        if (playerState == null) {
            return false;
        }

        return playerState.isDisabled();
    }

    public boolean isPlayerDisconnected(Player player) {
        PlayerState playerState = states.get(player.getUUID());
        if (playerState == null) {
            return true;
        }

        return playerState.isDisconnected();
    }

    public void syncOwnState() {
        CommonCompatibilityManager.INSTANCE.getNetManager().sendToServer(new PlayerStatePacket(state));
    }

    public boolean isDisabled() {
        return state.isDisabled();
    }

    public boolean isDisconnected() {
        return state.isDisconnected();
    }

    public void setDisabled(boolean disabled) {
        state.setDisabled(disabled);
        VoicechatClient.CLIENT_CONFIG.disabled.set(disabled).save();
        syncOwnState();
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        VoicechatClient.CLIENT_CONFIG.muted.set(muted).save();
    }

    public boolean isInGroup() {
        return getGroup() != null;
    }

    public boolean isInGroup(Player player) {
        PlayerState state = states.get(player.getUUID());
        if (state == null) {
            return false;
        }
        return state.hasGroup();
    }

    @Nullable
    public String getGroup(Player player) {
        PlayerState state = states.get(player.getUUID());
        if (state == null) {
            return null;
        }
        return state.getGroup();
    }

    @Nullable
    public String getGroup() {
        return state.getGroup();
    }

    public void setGroup(@Nullable String group) {
        state.setGroup(group);
        syncOwnState();
    }

    public List<PlayerState> getPlayerStates() {
        return new ArrayList<>(states.values());
    }

    @Nullable
    public PlayerState getState(UUID player) {
        return states.get(player);
    }

    public void clearStates() {
        states.clear();
    }
}
