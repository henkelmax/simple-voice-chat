package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Player;

public class PlayerImpl extends EntityImpl implements Player {

    public PlayerImpl(net.minecraft.world.entity.player.Player entity) {
        super(entity);
    }

    @Override
    public Object getPlayer() {
        return entity;
    }

    public net.minecraft.world.entity.player.Player getRealPlayer() {
        return (net.minecraft.world.entity.player.Player) entity;
    }

}
