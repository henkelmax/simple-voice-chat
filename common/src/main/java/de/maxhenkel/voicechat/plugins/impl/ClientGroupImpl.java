package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.voice.common.ClientGroup;

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

    public ClientGroup getGroup() {
        return group;
    }

}
