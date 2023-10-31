package de.maxhenkel.voicechat.permission;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

public class NeoForgePermission implements Permission {

    private final PermissionNode<Boolean> node;
    private final PermissionType type;

    public NeoForgePermission(PermissionNode<Boolean> node, PermissionType type) {
        this.node = node;
        this.type = type;
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return PermissionAPI.getPermission(player, node);
    }

    @Override
    public PermissionType getPermissionType() {
        return type;
    }

    public PermissionNode<Boolean> getNode() {
        return node;
    }
}
