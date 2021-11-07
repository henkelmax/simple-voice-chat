package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class VoicechatConnectionImpl implements VoicechatConnection {

    private final ServerPlayer player;
    private final PlayerState state;
    @Nullable
    private final Group group;

    public VoicechatConnectionImpl(ServerPlayer player, PlayerState state) {
        this.player = player;
        this.state = state;
        this.group = GroupImpl.create(state);
    }

    @Nullable
    public static VoicechatConnectionImpl fromPlayer(ServerPlayer player) {
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
        if (group == null) {
            server.getGroupManager().joinGroup(null, player, null);
            return;
        }
        if (group instanceof GroupImpl g) {
            de.maxhenkel.voicechat.voice.server.Group actualGroup = server.getGroupManager().getGroup(g.getGroup().getId());
            if (actualGroup == null) {
                server.getGroupManager().addGroup(g.getGroup(), player);
                actualGroup = g.getGroup();
            }
            server.getGroupManager().joinGroup(actualGroup, player, g.getGroup().getPassword());
        }
    }

    @Override
    public boolean isDisabled() {
        return state.isDisabled();
    }

    @Override
    public ServerPlayer getPlayer() {
        return player;
    }


}
