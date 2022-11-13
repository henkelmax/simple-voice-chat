package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class ServerPlayerImpl extends PlayerImpl implements ServerPlayer {

    public ServerPlayerImpl(EntityPlayerMP entity) {
        super(entity);
    }

    public EntityPlayerMP getRealServerPlayer() {
        return (EntityPlayerMP) entity;
    }

    @Override
    public ServerLevel getServerLevel() {
        return new ServerLevelImpl((WorldServer) entity.world);
    }
}
