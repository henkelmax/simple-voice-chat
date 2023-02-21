package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Group;
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

    @Override
    public boolean isPersistent() {
        return group.isPersistent();
    }

    @Override
    public Type getType() {
        return group.getType();
    }

    public de.maxhenkel.voicechat.voice.server.Group getGroup() {
        return group;
    }

    @Nullable
    public static GroupImpl create(PlayerState state) {
        UUID groupId = state.getGroup();
        Server server = Voicechat.SERVER.getServer();
        if (server != null && groupId != null) {
            de.maxhenkel.voicechat.voice.server.Group g = server.getGroupManager().getGroup(groupId);
            if (g != null) {
                return new GroupImpl(g);
            }
        }
        return null;
    }

    public static class BuilderImpl implements Group.Builder {

        private String name;
        @Nullable
        private String password;
        private boolean persistent;
        private Type type;

        public BuilderImpl() {
            type = Type.NORMAL;
        }

        @Override
        public Group.Builder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Group.Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        @Override
        public Group.Builder setPersistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        @Override
        public Group.Builder setType(Type type) {
            this.type = type;
            return this;
        }

        @Override
        public Group build() {
            if (name == null) {
                throw new IllegalStateException("name missing");
            }
            GroupImpl group = new GroupImpl(new de.maxhenkel.voicechat.voice.server.Group(UUID.randomUUID(), name, password, persistent, type));
            Server server = Voicechat.SERVER.getServer();
            if (server != null && persistent) {
                server.getGroupManager().addGroup(group.getGroup(), null);
            }
            return group;
        }
    }

    public static class TypeImpl implements Group.Type {

        public static short toInt(Group.Type type) {
            if (type == OPEN) {
                return 1;
            } else if (type == ISOLATED) {
                return 2;
            } else {
                return 0;
            }
        }

        public static Group.Type fromInt(short i) {
            if (i == 1) {
                return OPEN;
            } else if (i == 2) {
                return ISOLATED;
            } else {
                return NORMAL;
            }
        }
    }

}
