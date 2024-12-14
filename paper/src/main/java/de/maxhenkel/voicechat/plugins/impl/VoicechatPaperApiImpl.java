package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.*;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class VoicechatPaperApiImpl extends VoicechatServerApiImpl {

    @Deprecated
    public static final VoicechatPaperApiImpl PAPER_INSTANCE = new VoicechatPaperApiImpl();

    @Override
    public Entity fromEntity(Object entity) {
        if (entity instanceof net.minecraft.world.entity.Entity e) {
            return new EntityImpl(e);
        } else if (entity instanceof CraftEntity e) {
            return new EntityImpl(e.getHandle());
        } else {
            throw new IllegalArgumentException("entity is not an instance of Entity or CraftEntity");
        }
    }

    @Override
    public ServerLevel fromServerLevel(Object serverLevel) {
        if (serverLevel instanceof net.minecraft.server.level.ServerLevel l) {
            return new ServerLevelImpl(l);
        } else if (serverLevel instanceof CraftWorld l) {
            return new ServerLevelImpl(l.getHandle());
        } else {
            throw new IllegalArgumentException("serverLevel is not an instance of ServerLevel or CraftWorld");
        }
    }

    @Override
    public ServerPlayer fromServerPlayer(Object serverPlayer) {
        if (serverPlayer instanceof net.minecraft.server.level.ServerPlayer p) {
            return new ServerPlayerImpl(p);
        } else if (serverPlayer instanceof CraftPlayer p) {
            return new ServerPlayerImpl(p.getHandle());
        } else {
            throw new IllegalArgumentException("serverPlayer is not an instance of ServerPlayer or CraftPlayer");
        }
    }

}
