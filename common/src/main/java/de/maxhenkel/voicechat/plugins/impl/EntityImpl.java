package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Entity;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.intercompatibility.MinecraftCompatibilityManager;
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
        return MinecraftCompatibilityManager.fromVec3(entity.position());
    }

    @Override
    public Position getEyePosition() {
        return MinecraftCompatibilityManager.fromVec3(entity.getEyePosition());
    }

    @Override
    public ServerLevel getLevel() {
        Level level = entity.level();
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel)
            return MinecraftCompatibilityManager.fromServerLevel(serverLevel);
        else throw new IllegalStateException("Tried to access getServerLevel() on client!");
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
