package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.EnterPasswordScreen;
import de.maxhenkel.voicechat.gui.GroupScreen;
import de.maxhenkel.voicechat.gui.JoinGroupScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.UpdateStatePacket;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ClientPlayerStateManager {

    private boolean disconnected;
    @Nullable
    private ClientGroup group;

    private Map<UUID, PlayerState> states;

    public ClientPlayerStateManager() {
        this.disconnected = true;
        this.group = null;

        states = new HashMap<>();

        CommonCompatibilityManager.INSTANCE.getNetManager().playerStateChannel.setClientListener((client, handler, packet) -> {
            states.put(packet.getPlayerState().getUuid(), packet.getPlayerState());
            Voicechat.logDebug("Got state for {}: {}", packet.getPlayerState().getName(), packet.getPlayerState());
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().playerStatesChannel.setClientListener((client, handler, packet) -> {
            states = packet.getPlayerStates();
            Voicechat.logDebug("Received {} states", states.size());
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().joinedGroupChannel.setClientListener((client, handler, packet) -> {
            Screen screen = Minecraft.getInstance().screen;
            ClientGroup group = packet.getGroup();
            if (packet.isWrongPassword()) {
                if (screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                    Minecraft.getInstance().setScreen(null);
                }
                client.player.displayClientMessage(new TranslatableComponent("message.voicechat.wrong_password").withStyle(ChatFormatting.DARK_RED), true);
            } else if (group != null && screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                Minecraft.getInstance().setScreen(new GroupScreen(group));
            }
            this.group = group;
        });
        ClientCompatibilityManager.INSTANCE.onVoiceChatConnected(this::onVoiceChatConnected);
        ClientCompatibilityManager.INSTANCE.onVoiceChatDisconnected(this::onVoiceChatDisconnected);
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::onDisconnect);
    }

    private void resetOwnState() {
        disconnected = true;
        group = null;
    }

    /**
     * Called when the voicechat client gets disconnected or the player logs out
     */
    public void onVoiceChatDisconnected() {
        disconnected = true;
        syncOwnState();
    }

    /**
     * Called when the voicechat client gets (re)connected
     */
    public void onVoiceChatConnected(ClientVoicechatConnection client) {
        disconnected = false;
        syncOwnState();
    }

    private void onDisconnect() {
        clearStates();
        resetOwnState();
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
        NetManager.sendToServer(new UpdateStatePacket(disconnected, isDisabled()));
        Voicechat.logDebug("Sent own state to server: disconnected={}, disabled={}", disconnected, isDisabled());
    }

    public boolean isDisabled() {
        if (!canEnable()) {
            return true;
        }
        return VoicechatClient.CLIENT_CONFIG.disabled.get();
    }

    public boolean canEnable() {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return false;
        }
        return client.getSoundManager() != null;
    }

    public void setDisabled(boolean disabled) {
        VoicechatClient.CLIENT_CONFIG.disabled.set(disabled).save();
        syncOwnState();
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public boolean isMuted() {
        return VoicechatClient.CLIENT_CONFIG.muted.get();
    }

    public void setMuted(boolean muted) {
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
    public ClientGroup getGroup(Player player) {
        PlayerState state = states.get(player.getUUID());
        if (state == null) {
            return null;
        }
        return state.getGroup();
    }

    @Nullable
    public ClientGroup getGroup() {
        return group;
    }

    public List<PlayerState> getPlayerStates(boolean includeSelf) {
        if (includeSelf) {
            return new ArrayList<>(states.values());
        } else {
            return states.values().stream().filter(playerState -> !playerState.getUuid().equals(getOwnID())).collect(Collectors.toList());
        }
    }

    public UUID getOwnID() {
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            ClientVoicechatConnection connection = client.getConnection();
            if (connection != null) {
                return connection.getData().getPlayerUUID();
            }
        }
        return Minecraft.getInstance().getUser().getGameProfile().getId();
    }

    @Nullable
    public PlayerState getState(UUID player) {
        return states.get(player);
    }

    public void clearStates() {
        states.clear();
    }
}
