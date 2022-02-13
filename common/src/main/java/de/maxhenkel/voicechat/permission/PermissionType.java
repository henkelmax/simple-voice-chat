package de.maxhenkel.voicechat.permission;

import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

public enum PermissionType {

    EVERYONE, NOONE, OPS;

    boolean hasPermission(@Nullable ServerPlayerEntity player) {
        switch (this) {
            case EVERYONE:
                return true;
            default:
            case NOONE:
                return false;
            case OPS:
                return player != null && player.hasPermissions(player.server.getOperatorUserPermissionLevel());
        }
    }

}
