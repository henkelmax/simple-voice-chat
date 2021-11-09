package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Entity;
import de.maxhenkel.voicechat.api.Position;

import java.util.UUID;

public class EntityImpl implements Entity {

    protected org.bukkit.entity.Entity entity;

    public EntityImpl(org.bukkit.entity.Entity entity) {
        this.entity = entity;
    }

    @Override
    public UUID getUuid() {
        return entity.getUniqueId();
    }

    @Override
    public Object getEntity() {
        return entity;
    }

    @Override
    public Position getPosition() {
        return new PositionImpl(entity.getLocation());
    }

    public org.bukkit.entity.Entity getRealEntity() {
        return entity;
    }

}
