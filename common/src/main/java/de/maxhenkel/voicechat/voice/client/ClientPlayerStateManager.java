package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent;
import de.maxhenkel.voicechat.api.events.VoicechatDisableEvent;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.EnterPasswordScreen;
import de.maxhenkel.voicechat.gui.group.GroupList;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ClientPlayerStateManager {

    private boolean disconnected;
    @Nullable
    private UUID group;

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
            GroupList.update();
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().playerStatesChannel.setClientListener((client, handler, packet) -> {
            states = packet.getPlayerStates();
            Voicechat.logDebug("Received {} state(s)", states.size());
            for (PlayerState state : states.values()) {
                VoicechatClient.USERNAME_CACHE.updateUsername(state.getUuid(), state.getName());
            }
            VoicechatClient.USERNAME_CACHE.save();
            AdjustVolumeList.update();
            JoinGroupList.update();
            GroupList.update();
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().joinedGroupChannel.setClientListener((client, handler, packet) -> {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            this.group = packet.getGroup();
            if (packet.isWrongPassword()) {
                if (screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }
                client.player.sendStatusMessage(new TextComponentTranslation("message.voicechat.wrong_password").setStyle(new Style().setColor(TextFormatting.DARK_RED)), true);
            } else if (group != null && screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                ClientGroup clientGroup = getGroup();
                if (clientGroup != null) {
                    Minecraft.getMinecraft().displayGuiScreen(new GroupScreen(clientGroup));
                } else {
                    Voicechat.LOGGER.warn("Received join group packet without group being present");
                }
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

    public boolean isPlayerDisabled(EntityPlayer player) {
        PlayerState playerState = states.get(player.getUniqueID());
        if (playerState == null) {
            return false;
        }

        return playerState.isDisabled();
    }

    public boolean isPlayerDisconnected(EntityPlayer player) {
        PlayerState playerState = states.get(player.getUniqueID());
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
        return true; // client.getSoundManager() != null;
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

    public boolean isInGroup(EntityPlayer player) {
        PlayerState state = states.get(player.getUniqueID());
        if (state == null) {
            return false;
        }
        return state.hasGroup();
    }

    @Nullable
    public UUID getGroup(EntityPlayer player) {
        PlayerState state = states.get(player.getUniqueID());
        if (state == null) {
            return null;
        }
        return state.getGroup();
    }

    @Nullable
    public ClientGroup getGroup() {
        if (group == null) {
            return null;
        }
        return ClientManager.getGroupManager().getGroup(group);
    }

    @Nullable
    public UUID getGroupID() {
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
        return Minecraft.getMinecraft().getSession().getProfile().getId();
    }

    @Nullable
    public PlayerState getState(UUID player) {
        return states.get(player);
    }

    public void clearStates() {
        states.clear();
    }
}
