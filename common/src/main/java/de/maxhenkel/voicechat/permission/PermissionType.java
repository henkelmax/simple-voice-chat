package de.maxhenkel.voicechat.permission;

import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;

public enum PermissionType {

    EVERYONE, NOONE, OPS;

    boolean hasPermission(@Nullable EntityPlayerMP player) {
        switch (this) {
            case EVERYONE:
                return true;
            default:
            case NOONE:
                return false;
            case OPS:
                return player != null && player.mcServer.getPlayerList().canSendCommands(player.getGameProfile());
        }
    }

}
