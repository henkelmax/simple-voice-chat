package de.maxhenkel.voicechat.permission;

import net.minecraft.server.level.ServerPlayer;
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
            case NOONE -> level = DefaultPermissionLevel.NONE;
            case EVERYONE -> level = DefaultPermissionLevel.ALL;
            case OPS -> level = DefaultPermissionLevel.OP;
        }
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
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
