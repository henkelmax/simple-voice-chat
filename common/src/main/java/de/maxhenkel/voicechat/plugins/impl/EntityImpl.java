package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Entity;
import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.UUID;

public class EntityImpl implements Entity {

    protected net.minecraft.world.entity.Entity entity;

    public EntityImpl(net.minecraft.world.entity.Entity entity) {
        this.entity = entity;
    }

    @Override
    public UUID getUuid() {
        return entity.getUUID();
    }

    @Override
    public Object getEntity() {
        return entity;
    }

    @Override
    public Position getPosition() {
        return Voicechat.outSourcing.getServerApi().fromVec3(entity.position());
    }

    @Override
    public ServerLevel getServerLevel() {
        Level level = entity.level();
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel)
            return Voicechat.outSourcing.getServerApi().fromServerLevel(serverLevel);
        else throw new IllegalStateException("Tried to access getServerLevel() on client!");
    }

    @Override
    public Position getEyePosition() {
        return Voicechat.outSourcing.getServerApi().fromVec3(entity.getEyePosition());
    }

    @Override
    public boolean isSpectator() {
        return entity.isSpectator();
    }

    @Override
    public MinecraftServer getServer() {
        return Voicechat.outSourcing.getServerApi().fromServer(entity.getServer());
    }

    @Override
    public boolean hasPermissions(int operatorUserPermissionLevel) {
        return entity.hasPermissions(operatorUserPermissionLevel);
    }

    public net.minecraft.world.entity.Entity getRealEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        EntityImpl entity1 = (EntityImpl) object;
        return Objects.equals(entity, entity1.entity);
    }

    @Override
    public int hashCode() {
        return entity != null ? entity.hashCode() : 0;
    }
}
