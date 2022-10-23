package de.maxhenkel.voicechat.permission;

import de.maxhenkel.voicechat.Voicechat;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class FabricPermissionManager extends PermissionManager {

    @Override
    public Permission createPermissionInternal(String modId, String node, PermissionType type) {
        return new Permission() {
            @Override
            public boolean hasPermission(ServerPlayer player) {
                if (isFabricPermissionsAPILoaded()) {
                    return Permissions.check(player, modId + "." + node, type.hasPermission(player));
                }
                return type.hasPermission(player);
            }

            @Override
            public PermissionType getPermissionType() {
                return type;
            }
        };
    }

    private static Boolean loaded;

    private static boolean isFabricPermissionsAPILoaded() {
        if (loaded == null) {
            loaded = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
            if (loaded) {
                Voicechat.LOGGER.info("Using Fabric Permissions API");
            }
        }
        return loaded;
    }

}
