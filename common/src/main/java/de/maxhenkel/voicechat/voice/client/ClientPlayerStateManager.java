package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent;
import de.maxhenkel.voicechat.api.events.VoicechatDisableEvent;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.EnterPasswordScreen;
import de.maxhenkel.voicechat.gui.group.GroupScreen;
import de.maxhenkel.voicechat.gui.group.JoinGroupList;
import de.maxhenkel.voicechat.gui.group.JoinGroupScreen;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeList;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.UpdateStatePacket;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.events.ClientVoicechatConnectionEventImpl;
import de.maxhenkel.voicechat.plugins.impl.events.MicrophoneMuteEventImpl;
import de.maxhenkel.voicechat.plugins.impl.events.VoicechatDisableEventImpl;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ClientPlayerStateManager {

    private boolean disconnected;
    // TODO Maybe change to UUID
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
            VoicechatClient.USERNAME_CACHE.updateUsernameAndSave(packet.getPlayerState().getUuid(), packet.getPlayerState().getName());
            AdjustVolumeList.update();
            JoinGroupList.update();
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().playerStatesChannel.setClientListener((client, handler, packet) -> {
            states = packet.getPlayerStates();
            Voicechat.logDebug("Received {} states", states.size());
            for (PlayerState state : states.values()) {
                VoicechatClient.USERNAME_CACHE.updateUsername(state.getUuid(), state.getName());
            }
            VoicechatClient.USERNAME_CACHE.save();
            AdjustVolumeList.update();
            JoinGroupList.update();
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().joinedGroupChannel.setClientListener((client, handler, packet) -> {
            Screen screen = Minecraft.getInstance().screen;
            this.group = packet.getGroup();
            if (packet.isWrongPassword()) {
                if (screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                    Minecraft.getInstance().setScreen(null);
                }
                client.player.displayClientMessage(Component.translatable("message.voicechat.wrong_password").withStyle(ChatFormatting.DARK_RED), true);
            } else if (group != null && screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                Minecraft.getInstance().setScreen(new GroupScreen(group));
            }
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
        PluginManager.instance().dispatchEvent(ClientVoicechatConnectionEvent.class, new ClientVoicechatConnectionEventImpl(false));
    }

    /**
     * Called when the voicechat client gets (re)connected
     */
    public void onVoiceChatConnected(ClientVoicechatConnection client) {
        disconnected = false;
        syncOwnState();
        PluginManager.instance().dispatchEvent(ClientVoicechatConnectionEvent.class, new ClientVoicechatConnectionEventImpl(true));
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
            return VoicechatClient.CLIENT_CONFIG.showFakePlayersDisconnected.get();
        }

        return playerState.isDisconnected();
    }

    public void syncOwnState() {
        NetManager.sendToServer(new UpdateStatePacket(isDisabled()));
        Voicechat.logDebug("Sent own state to server: disabled={}", isDisabled());
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
        PluginManager.instance().dispatchEvent(VoicechatDisableEvent.class, new VoicechatDisableEventImpl(disabled));
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public boolean isMuted() {
        return VoicechatClient.CLIENT_CONFIG.muted.get();
    }

    public void setMuted(boolean muted) {
        VoicechatClient.CLIENT_CONFIG.muted.set(muted).save();
        PluginManager.instance().dispatchEvent(MicrophoneMuteEvent.class, new MicrophoneMuteEventImpl(muted));
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
    public UUID getGroup(Player player) {
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
