package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Entity;
import de.maxhenkel.voicechat.api.Position;

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
        return new PositionImpl(entity.position());
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
