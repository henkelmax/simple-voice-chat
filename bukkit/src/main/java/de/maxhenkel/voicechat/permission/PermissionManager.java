package de.maxhenkel.voicechat.permission;

import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PermissionManager {

    public static Permission LISTEN_PERMISSION = new Permission(Voicechat.MODID + ".listen", PermissionDefault.TRUE);
    public static Permission SPEAK_PERMISSION = new Permission(Voicechat.MODID + ".speak", PermissionDefault.TRUE);
    public static Permission GROUPS_PERMISSION = new Permission(Voicechat.MODID + ".groups", PermissionDefault.TRUE);
    public static Permission ADMIN_PERMISSION = new Permission(Voicechat.MODID + ".admin", PermissionDefault.OP);

}
