package de.maxhenkel.voicechat.permission;

import net.minecraft.entity.player.EntityPlayerMP;

public interface Permission {

    boolean hasPermission(EntityPlayerMP player);

    PermissionType getPermissionType();

}
