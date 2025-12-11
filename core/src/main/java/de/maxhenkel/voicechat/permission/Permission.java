package de.maxhenkel.voicechat.permission;

import de.maxhenkel.voicechat.api.ServerPlayer;

public interface Permission {

    boolean hasPermission(ServerPlayer player);

    PermissionType getPermissionType();

}
