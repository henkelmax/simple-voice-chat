package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Player;

public class PlayerImpl extends EntityImpl implements Player {

    public PlayerImpl(net.minecraft.world.entity.player.Player entity) {
        super(entity);
    }

    @Override
    public String getName() {
        return entity.getName().getString();
    }

}
