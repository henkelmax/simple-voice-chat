package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Player;

public class PlayerImpl extends EntityImpl implements Player {

    public PlayerImpl(org.bukkit.entity.Player entity) {
        super(entity);
    }

    @Override
    public Object getPlayer() {
        return entity;
    }

    public org.bukkit.entity.Player getRealPlayer() {
        return (org.bukkit.entity.Player) entity;
    }

}
