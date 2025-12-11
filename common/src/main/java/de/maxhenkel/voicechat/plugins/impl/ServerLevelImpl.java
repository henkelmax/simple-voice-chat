package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.intercompatibility.UncommonCompatibilityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ServerLevelImpl implements ServerLevel {

    private final net.minecraft.server.level.ServerLevel serverLevel;

    public ServerLevelImpl(net.minecraft.server.level.ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    @Override
    public Object getServerLevel() {
        return serverLevel;
    }

    @Override
    public List<ServerPlayer> players() {
        return serverLevel.players().stream().map(player -> UncommonCompatibilityManager.INSTANCE.getServerApi().fromServerPlayer(player)).collect(Collectors.toCollection(ArrayList::new));
    }

    public net.minecraft.server.level.ServerLevel getRawServerLevel() {
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
