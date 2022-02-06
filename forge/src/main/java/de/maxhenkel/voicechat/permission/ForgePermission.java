package de.maxhenkel.voicechat.permission;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.nodes.PermissionNode;

public class ForgePermission implements Permission {

    private final PermissionNode<Boolean> node;
    private final PermissionType type;

    public ForgePermission(PermissionNode<Boolean> node, PermissionType type) {
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
