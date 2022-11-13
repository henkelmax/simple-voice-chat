package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ServerLevel;
import net.minecraft.world.WorldServer;

public class ServerLevelImpl implements ServerLevel {

    private final WorldServer serverLevel;

    public ServerLevelImpl(WorldServer serverLevel) {
        this.serverLevel = serverLevel;
    }

    @Override
    public Object getServerLevel() {
        return serverLevel;
    }
}
