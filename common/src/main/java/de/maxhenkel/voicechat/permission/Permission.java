package de.maxhenkel.voicechat.permission;

import net.minecraft.server.level.ServerPlayer;

public interface Permission {

    boolean hasPermission(ServerPlayer player);

    PermissionType getPermissionType();

}
