package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ServerLevel;
import net.minecraft.world.WorldServer;

import java.util.Objects;

public class ServerLevelImpl implements ServerLevel {

    private final WorldServer serverLevel;

    public ServerLevelImpl(WorldServer serverLevel) {
        this.serverLevel = serverLevel;
    }

    @Override
    public Object getServerLevel() {
        return serverLevel;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ServerLevelImpl that = (ServerLevelImpl) object;
        return Objects.equals(serverLevel, that.serverLevel);
    }

    @Override
    public int hashCode() {
        return serverLevel != null ? serverLevel.hashCode() : 0;
    }
}
