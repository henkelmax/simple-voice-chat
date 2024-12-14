package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerImpl extends EntityImpl implements Player {

    public PlayerImpl(PlayerEntity entity) {
        super(entity);
    }

    @Override
    public Object getPlayer() {
        return CommonCompatibilityManager.INSTANCE.createRawApiPlayer(getRealPlayer());
    }

    public PlayerEntity getRealPlayer() {
        return (PlayerEntity) entity;
    }

}
