package de.maxhenkel.voicechat.permission;

import net.minecraftforge.server.permission.PermissionAPI;

public class ForgePermissionManager extends PermissionManager {

    @Override
    public Permission createPermissionInternal(String modId, String node, PermissionType type) {
        return new ForgePermission(modId + "." + node, type);
    }

    public void registerPermissions() {
        getPermissions().stream().map(ForgePermission.class::cast).forEach(permission -> {
            PermissionAPI.registerNode(permission.getNode(), permission.getLevel(), "");
        });
    }

}
