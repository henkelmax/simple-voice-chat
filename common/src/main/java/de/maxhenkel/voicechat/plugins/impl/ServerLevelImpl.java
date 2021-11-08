package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ServerLevel;

public class ServerLevelImpl implements ServerLevel {

    private final net.minecraft.server.level.ServerLevel serverLevel;

    public ServerLevelImpl(net.minecraft.server.level.ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    @Override
    public Object getServerLevel() {
        return serverLevel;
    }
}
