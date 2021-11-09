package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.Server;

import javax.annotation.Nullable;
import java.util.UUID;

public class GroupImpl implements Group {

    private final de.maxhenkel.voicechat.voice.server.Group group;

    public GroupImpl(de.maxhenkel.voicechat.voice.server.Group group) {
        this.group = group;
    }

    @Override
    public String getName() {
        return group.getName();
    }

    @Override
    public boolean hasPassword() {
        return group.getPassword() != null;
    }

    @Override
    public UUID getId() {
        return group.getId();
    }

    public de.maxhenkel.voicechat.voice.server.Group getGroup() {
        return group;
    }

    @Nullable
    public static GroupImpl create(PlayerState state) {
        ClientGroup group = state.getGroup();
        Server server = Voicechat.SERVER.getServer();
        if (server != null && group != null) {
            de.maxhenkel.voicechat.voice.server.Group g = server.getGroupManager().getGroup(group.getId());
            if (g != null) {
                return new GroupImpl(g);
            }
        }
        return null;
    }

}
