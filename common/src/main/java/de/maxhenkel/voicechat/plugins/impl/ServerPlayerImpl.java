package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.intercompatibility.MinecraftCompatibilityManager;
import net.minecraft.world.entity.Entity;

public class ServerPlayerImpl extends PlayerImpl implements ServerPlayer {

    public ServerPlayerImpl(net.minecraft.server.level.ServerPlayer entity) {
        super(entity);
    }

    @Override
    public String getName() {
        return entity.getName().getString();
    }

    @Override
    public ServerPlayer getCameraPlayer() {
        Entity camera = ((net.minecraft.server.level.ServerPlayer)entity).getCamera();
        if (camera instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            return MinecraftCompatibilityManager.fromServerPlayer(serverPlayer);
        else
            return null;
    }

    @Override
    public boolean isSpectator() {
        return entity.isSpectator();
    }

    @Override
    public boolean hasPermissions(int operatorUserPermissionLevel) {
        return entity.hasPermissions(operatorUserPermissionLevel);
    }
}
