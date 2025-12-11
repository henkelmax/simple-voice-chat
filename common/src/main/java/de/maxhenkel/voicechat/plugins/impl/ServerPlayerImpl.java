package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.intercompatibility.UncommonCompatibilityManager;
import net.minecraft.world.entity.Entity;

public class ServerPlayerImpl extends PlayerImpl implements ServerPlayer {

    public ServerPlayerImpl(net.minecraft.server.level.ServerPlayer entity) {
        super(entity);
    }

    @Override
    public ServerLevel getServerLevel() {
        return UncommonCompatibilityManager.INSTANCE.getServerApi().fromServerLevel(entity.level());
    }

    @Override
    public ServerPlayer getCameraPlayer() {
        Entity camera = ((net.minecraft.server.level.ServerPlayer)entity).getCamera();
        if (camera instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            return UncommonCompatibilityManager.INSTANCE.getServerApi().fromServerPlayer(serverPlayer);
        else
            return null;
    }
}
