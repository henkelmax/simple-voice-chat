package de.maxhenkel.voicechat.permission;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.UncommonCompatibilityManager;
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
    public boolean hasPermission(de.maxhenkel.voicechat.api.ServerPlayer player) {
        return PermissionAPI.getPermission(((CommonCompatibilityManager) UncommonCompatibilityManager.INSTANCE).getServerPlayer(player), node);
    }

    @Override
    public PermissionType getPermissionType() {
        return type;
    }

    public PermissionNode<Boolean> getNode() {
        return node;
    }
}
