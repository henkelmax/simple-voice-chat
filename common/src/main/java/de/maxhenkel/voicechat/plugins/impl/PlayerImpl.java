package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;

public class PlayerImpl extends EntityImpl implements Player {

    public PlayerImpl(net.minecraft.world.entity.player.Player entity) {
        super(entity);
    }

    @Override
    public Object getPlayer() {
        return CommonCompatibilityManager.INSTANCE.createRawApiPlayer(getRealPlayer());
    }

    public net.minecraft.world.entity.player.Player getRealPlayer() {
        return (net.minecraft.world.entity.player.Player) entity;
    }

}
