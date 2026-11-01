package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.PlayerStateManager;
import de.maxhenkel.voicechat.voice.server.Server;

import javax.annotation.Nullable;

public class VoicechatConnectionImpl implements VoicechatConnection {

    private final ServerPlayer player;
    private final net.minecraft.server.level.ServerPlayer serverPlayer;
    @Nullable
    private final Group group;
    private final boolean connected;
    private final boolean muted;
    private final boolean forceMuted;
    private final boolean disabled;
    private final boolean forceDisabled;
    private final boolean installed;

    public VoicechatConnectionImpl(net.minecraft.server.level.ServerPlayer player, PlayerState state) {
        this.serverPlayer = player;
        this.player = new ServerPlayerImpl(player);
        this.group = GroupImpl.create(state);
        this.connected = !state.isDisconnected();
        this.muted = state.isMutedOrForceMuted();
        this.forceMuted = state.isForceMuted();
        this.disabled = state.isDisabledOrForceDisabled();
        this.forceDisabled = state.isForceDisabled();
        this.installed = Voicechat.SERVER.isCompatible(player);
    }

    @Nullable
    public static VoicechatConnectionImpl fromPlayer(@Nullable net.minecraft.server.level.ServerPlayer player) {
        if (player == null) {
            return null;
        }
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return null;
        }
        PlayerState state = server.getPlayerStateManager().getState(player.getUUID());
        if (state == null) {
            return null;
        }
        return new VoicechatConnectionImpl(player, state);
    }

    @Nullable
    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public boolean isInGroup() {
        return group != null;
    }

    @Override
    public void setGroup(@Nullable Group group) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        server.getPlayerStateManager().checkIfStateCanBeModified();
        if (group == null) {
            server.getGroupManager().leaveGroup(serverPlayer);
            return;
        }
        if (group instanceof GroupImpl g) {
            de.maxhenkel.voicechat.voice.server.Group actualGroup = server.getGroupManager().getGroup(g.getGroup().getId());
            if (actualGroup == null) {
                server.getGroupManager().addGroup(g.getGroup(), serverPlayer);
                actualGroup = g.getGroup();
            }
            server.getGroupManager().joinGroup(actualGroup, serverPlayer, g.getGroup().getPassword());
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void setConnected(boolean connected) {
        if (isInstalled()) {
            return;
        }
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        PlayerStateManager manager = server.getPlayerStateManager();
        manager.checkIfStateCanBeModified();
        PlayerState actualState = manager.getState(serverPlayer.getUUID());
        if (actualState == null) {
            return;
        }
        if (actualState.isDisconnected() != connected) {
            return;
        }
        actualState.setDisconnected(!connected);
        manager.broadcastState(serverPlayer, actualState);
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    @Override
    public void setMuted(boolean muted) {
        if (isInstalled()) {
            return;
        }
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        PlayerStateManager manager = server.getPlayerStateManager();
        manager.checkIfStateCanBeModified();
        PlayerState actualState = manager.getState(serverPlayer.getUUID());
        if (actualState == null) {
            return;
        }
        if (actualState.isMuted() == muted) {
            return;
        }
        actualState.setMuted(muted);
        manager.broadcastState(serverPlayer, actualState);
    }

    @Override
    public boolean isForceMuted() {
        return forceMuted;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        if (isInstalled()) {
            return;
        }
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        PlayerStateManager manager = server.getPlayerStateManager();
        manager.checkIfStateCanBeModified();
        PlayerState actualState = manager.getState(serverPlayer.getUUID());
        if (actualState == null) {
            return;
        }
        if (actualState.isDisabled() == disabled) {
            return;
        }
        actualState.setDisabled(disabled);
        manager.broadcastState(serverPlayer, actualState);
    }

    @Override
    public boolean isForceDisabled() {
        return forceDisabled;
    }

    @Override
    public void evaluateForcedState() {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        server.getPlayerStateManager().evaluateForcedState(serverPlayer);
    }

    @Override
    public void evaluateVisibility() {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        server.getPlayerStateManager().evaluateVisibility(serverPlayer);
    }

    @Override
    public boolean isInstalled() {
        return installed;
    }

    @Override
    public ServerPlayer getPlayer() {
        return player;
    }

}
