package de.maxhenkel.voicechat.permission;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public enum PermissionType {

    EVERYONE, NOONE, OPS;

    boolean hasPermission(@Nullable ServerPlayer player) {
        return switch (this) {
            case EVERYONE -> true;
            case NOONE -> false;
            case OPS -> player != null && player.hasPermissions(player.server.getOperatorUserPermissionLevel());
        };
    }

}
