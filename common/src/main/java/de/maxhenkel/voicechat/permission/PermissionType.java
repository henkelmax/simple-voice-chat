package de.maxhenkel.voicechat.permission;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import javax.annotation.Nullable;

public enum PermissionType {

    EVERYONE, NOONE, OPS;

    boolean hasPermission(@Nullable ServerPlayer player) {
        return switch (this) {
            case EVERYONE -> true;
            case NOONE -> false;
            case OPS ->
                    player != null && player.permissions().hasPermission(new net.minecraft.server.permissions.Permission.HasCommandLevel(PermissionLevel.ADMINS));
        };
    }

}
