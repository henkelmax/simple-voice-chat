package de.maxhenkel.voicechat.permission;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PermissionManager {

    public static Permission CONNECT_PERMISSION = new Permission("voicechat.connect", PermissionDefault.TRUE);
    public static Permission SPEAK_PERMISSION = new Permission("voicechat.speak", PermissionDefault.TRUE);
    public static Permission GROUPS_PERMISSION = new Permission("voicechat.groups", PermissionDefault.TRUE);
    public static Permission ADMIN_PERMISSION = new Permission("voicechat.admin", PermissionDefault.OP);

}
