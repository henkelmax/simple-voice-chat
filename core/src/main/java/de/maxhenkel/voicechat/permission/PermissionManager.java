package de.maxhenkel.voicechat.permission;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.intercompatibility.UncommonCompatibilityManager;

import java.util.ArrayList;
import java.util.List;

public abstract class PermissionManager {

    public static PermissionManager INSTANCE = UncommonCompatibilityManager.INSTANCE.createPermissionManager();

    public final Permission LISTEN_PERMISSION;
    public final Permission SPEAK_PERMISSION;
    public final Permission GROUPS_PERMISSION;
    public final Permission ADMIN_PERMISSION;

    protected List<Permission> permissions = new ArrayList<>();

    public PermissionManager() {
        LISTEN_PERMISSION = createPermission(Voicechat.MODID, "listen", PermissionType.EVERYONE);
        SPEAK_PERMISSION = createPermission(Voicechat.MODID, "speak", PermissionType.EVERYONE);
        GROUPS_PERMISSION = createPermission(Voicechat.MODID, "groups", PermissionType.EVERYONE);
        ADMIN_PERMISSION = createPermission(Voicechat.MODID, "admin", PermissionType.OPS);
    }

    public abstract Permission createPermissionInternal(String modId, String node, PermissionType type);

    public Permission createPermission(String modId, String node, PermissionType type) {
        Permission p = createPermissionInternal(modId, node, type);
        permissions.add(p);
        return p;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasGroupPermissions(ServerPlayer player) {
        if (!PermissionManager.INSTANCE.GROUPS_PERMISSION.hasPermission(player)) {
            UncommonCompatibilityManager.INSTANCE.getServerApi().displayCLientMessage(player, "message.voicechat.no_group_permission", true);
            return false;
        }
        return true;
    }

    public boolean hasSpeakPermissions(ServerPlayer player) {
        if (!PermissionManager.INSTANCE.SPEAK_PERMISSION.hasPermission(player)) {
            CooldownTimer.run("no-speak-" + player.getUuid(), 30_000L, () ->
                    UncommonCompatibilityManager.INSTANCE.getServerApi().displayCLientMessage(player, "message.voicechat.no_speak_permission", true));
            return false;
        }
        return true;
    }

    public boolean hasListenPermissions(ServerPlayer player) {
        if (!PermissionManager.INSTANCE.LISTEN_PERMISSION.hasPermission(player)) {
            CooldownTimer.run(String.format("no-listen-%s", player.getUuid()), 30_000L, () ->
                    UncommonCompatibilityManager.INSTANCE.getServerApi().displayCLientMessage(player, "message.voicechat.no_listen_permission", true));
            return false;
        }
        return true;
    }
}
