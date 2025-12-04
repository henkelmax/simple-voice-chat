package de.maxhenkel.voicechat.permission;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.dialstuff.MinimalPlayer;

public interface Permission {

    boolean hasPermission(ServerPlayer player);

    PermissionType getPermissionType();

}
