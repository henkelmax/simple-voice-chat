package de.maxhenkel.voicechat.permission;

import de.maxhenkel.voicechat.Voicechat;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

public class FabricPermissionManager extends PermissionManager {

    private static boolean v1working = true;

    @Override
    public Permission createPermissionInternal(String modId, String node, PermissionType type) {
        Identifier identifier = Identifier.fromNamespaceAndPath(modId, node);
        String permissionNode = modId + "." + node;
        return new Permission() {
            @Override
            public boolean hasPermission(ServerPlayer player) {
                if (v1working) {
                    try {
                        return player.checkPermission(identifier, fromPermissionType(type));
                    } catch (Throwable t) {
                        v1working = false;
                        Voicechat.LOGGER.warn("Failed to use fabric-permission-api-v1", t);
                    }
                }
                if (isFabricPermissionsAPIv0Loaded()) {
                    try {
                        return Permissions.check(player, permissionNode, type.hasPermission(player));
                    } catch (Throwable t) {
                        v0loaded = false;
                        Voicechat.LOGGER.warn("Failed to use fabric-permissions-api-v0", t);
                        Voicechat.LOGGER.info("Disabling fabric-permissions-api-v0 integration");
                    }
                }
                return type.hasPermission(player);
            }

            @Override
            public PermissionType getPermissionType() {
                return type;
            }
        };
    }

    private static Boolean v0loaded;

    private static boolean isFabricPermissionsAPIv0Loaded() {
        if (v0loaded == null) {
            v0loaded = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
            if (v0loaded) {
                Voicechat.LOGGER.info("Using Fabric Permissions API");
            }
        }
        return v0loaded;
    }

    private PermissionLevel fromPermissionType(PermissionType type) {
        return switch (type) {
            case OPS -> PermissionLevel.ADMINS;
            default -> PermissionLevel.ALL;
        };
    }

}
