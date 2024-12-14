package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.voice.common.ClientGroup;

import java.util.Objects;
import java.util.UUID;

public class ClientGroupImpl implements Group {

    private final ClientGroup group;

    public ClientGroupImpl(ClientGroup group) {
        this.group = group;
    }

    @Override
    public String getName() {
        return group.getName();
    }

    @Override
    public boolean hasPassword() {
        return group.hasPassword();
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
    public boolean isHidden() {
        return group.isHidden();
    }

    @Override
    public Type getType() {
        return group.getType();
    }

    public ClientGroup getGroup() {
        return group;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ClientGroupImpl that = (ClientGroupImpl) object;
        return Objects.equals(group.getId(), that.group.getId());
    }

    @Override
    public int hashCode() {
        return group != null ? group.getId().hashCode() : 0;
    }
}
