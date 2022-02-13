package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Player;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerImpl extends EntityImpl implements Player {

    public PlayerImpl(PlayerEntity entity) {
        super(entity);
    }

    @Override
    public Object getPlayer() {
        return entity;
    }

    public PlayerEntity getRealPlayer() {
        return (PlayerEntity) entity;
    }

}
