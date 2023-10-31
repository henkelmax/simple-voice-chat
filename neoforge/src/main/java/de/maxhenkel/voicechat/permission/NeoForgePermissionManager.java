package de.maxhenkel.voicechat.permission;

import java.util.stream.Collectors;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

public class NeoForgePermissionManager extends PermissionManager {

    @Override
    public Permission createPermissionInternal(String modId, String node, PermissionType type) {
        return new NeoForgePermission(new PermissionNode<>(modId, node, PermissionTypes.BOOLEAN, (player, playerUUID, context) -> type.hasPermission(player)), type);
    }

    @SubscribeEvent
    public void registerPermissions(PermissionGatherEvent.Nodes event) {
        event.addNodes(getPermissions().stream().map(NeoForgePermission.class::cast).map(NeoForgePermission::getNode).collect(Collectors.toList()));
    }

}
