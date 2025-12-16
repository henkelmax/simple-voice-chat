package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.intercompatibility.MinecraftCompatibilityManager;

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
        return serverLevel.players().stream().map(MinecraftCompatibilityManager::fromServerPlayer).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public String getResourceLocation() {
        return serverLevel.getLevel().dimension().location().toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ServerLevel serverLevel1)) {
            return false;
        }
        return Objects.equals(serverLevel, serverLevel1.getServerLevel());
    }

    @Override
    public int hashCode() {
        return serverLevel != null ? serverLevel.hashCode() : 0;
    }
}
