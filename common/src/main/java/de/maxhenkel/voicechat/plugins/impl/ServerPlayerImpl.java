package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ServerPlayerImpl extends PlayerImpl implements ServerPlayer {

    public ServerPlayerImpl(net.minecraft.server.level.ServerPlayer entity) {
        super(entity);
    }

    public net.minecraft.server.level.ServerPlayer getRealServerPlayer() {
        return (net.minecraft.server.level.ServerPlayer) entity;
    }

    @Override
    public ServerLevel getServerLevel() {
        return Voicechat.outSourcing.getServerApi().fromServerLevel(entity.level());
    }

    @Override
    public ServerPlayer getCameraPlayer() {
        Entity camera = ((net.minecraft.server.level.ServerPlayer)entity).getCamera();
        if (camera instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            return Voicechat.outSourcing.getServerApi().fromServerPlayer(serverPlayer);
        else
            return null;
    }
}
