package de.maxhenkel.voicechat.permission;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface Permission {

    boolean hasPermission(ServerPlayerEntity player);

    PermissionType getPermissionType();

}
