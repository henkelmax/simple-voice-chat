package de.maxhenkel.voicechat.permission;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PaperPermissionManager extends PermissionManager {

    @Override
    public de.maxhenkel.voicechat.permission.Permission createPermissionInternal(String modId, String node, PermissionType type) {
        return new PaperPermission(new Permission(modId + "." + node, map(type)), type);
    }

    private static PermissionDefault map(PermissionType type) {
        return switch (type) {
            case OPS -> PermissionDefault.OP;
            default -> PermissionDefault.TRUE;
        };
    }

}
