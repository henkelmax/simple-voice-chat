package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ServerLevel;
import net.minecraft.world.server.ServerWorld;

public class ServerLevelImpl implements ServerLevel {

    private final ServerWorld serverLevel;

    public ServerLevelImpl(ServerWorld serverLevel) {
        this.serverLevel = serverLevel;
    }

    @Override
    public Object getServerLevel() {
        return serverLevel;
    }
}
