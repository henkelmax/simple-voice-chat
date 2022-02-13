package de.maxhenkel.voicechat.permission;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class ForgePermission implements Permission {

    private final String node;
    private DefaultPermissionLevel level;
    private final PermissionType type;

    public ForgePermission(String node, PermissionType type) {
        this.node = node;
        this.type = type;
        switch (type) {
            case NOONE:
                level = DefaultPermissionLevel.NONE;
                break;
            case EVERYONE:
                level = DefaultPermissionLevel.ALL;
                break;
            case OPS:
                level = DefaultPermissionLevel.OP;
                break;
        }
    }

    @Override
    public boolean hasPermission(ServerPlayerEntity player) {
        return PermissionAPI.hasPermission(player, node);
    }

    @Override
    public PermissionType getPermissionType() {
        return type;
    }

    public String getNode() {
        return node;
    }

    public DefaultPermissionLevel getLevel() {
        return level;
    }
}
