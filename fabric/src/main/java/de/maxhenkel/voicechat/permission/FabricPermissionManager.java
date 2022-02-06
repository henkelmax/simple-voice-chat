package de.maxhenkel.voicechat.permission;

import net.minecraft.server.level.ServerPlayer;

public class FabricPermissionManager extends PermissionManager {

    @Override
    public Permission createPermissionInternal(String modId, String node, PermissionType type) {
        return new Permission() {
            @Override
            public boolean hasPermission(ServerPlayer player) {
                return type.hasPermission(player);
            }

            @Override
            public PermissionType getPermissionType() {
                return type;
            }
        };
    }

}
