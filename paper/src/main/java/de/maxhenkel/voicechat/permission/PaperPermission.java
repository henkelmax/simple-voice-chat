package de.maxhenkel.voicechat.permission;

import net.minecraft.server.level.ServerPlayer;

public class PaperPermission implements Permission {

    private final org.bukkit.permissions.Permission node;
    private final PermissionType type;

    public PaperPermission(org.bukkit.permissions.Permission node, PermissionType type) {
        this.node = node;
        this.type = type;
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.getBukkitEntity().hasPermission(node);
    }

    @Override
    public PermissionType getPermissionType() {
        return type;
    }

    public org.bukkit.permissions.Permission getNode() {
        return node;
    }
}
