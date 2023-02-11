package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.AddGroupPacket;
import de.maxhenkel.voicechat.net.JoinedGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.RemoveGroupPacket;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO Rename to ServerGroupManager
public class GroupManager {

    private final Map<UUID, Group> groups;
    private final Server server;

    public GroupManager(Server server) {
        this.server = server;
        groups = new ConcurrentHashMap<>();
        CommonCompatibilityManager.INSTANCE.onPlayerCompatibilityCheckSucceeded(this::onPlayerCompatibilityCheckSucceeded);
        CommonCompatibilityManager.INSTANCE.getNetManager().joinGroupChannel.setServerListener((srv, player, handler, packet) -> {
            if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
                return;
            }
            if (!PermissionManager.INSTANCE.GROUPS_PERMISSION.hasPermission(player)) {
                player.displayClientMessage(Component.translatable("message.voicechat.no_group_permission"), true);
                return;
            }
            joinGroup(groups.get(packet.getGroup()), player, packet.getPassword());
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().createGroupChannel.setServerListener((srv, player, handler, packet) -> {
            if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
                return;
            }
            if (!PermissionManager.INSTANCE.GROUPS_PERMISSION.hasPermission(player)) {
                player.displayClientMessage(Component.translatable("message.voicechat.no_group_permission"), true);
                return;
            }
            addGroup(new Group(UUID.randomUUID(), packet.getName(), packet.getPassword()), player);
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().leaveGroupChannel.setServerListener((srv, player, handler, packet) -> {
            leaveGroup(player);
        });
    }

    private void onPlayerCompatibilityCheckSucceeded(ServerPlayer player) {
        Voicechat.logDebug("Synchronizing {} groups with {}", groups.size(), player.getDisplayName().getString());
        for (Group category : groups.values()) {
            broadcastAddGroup(category);
        }
    }

    private PlayerStateManager getStates() {
        return server.getPlayerStateManager();
    }

    public void addGroup(Group group, @Nullable ServerPlayer player) {
        if (PluginManager.instance().onCreateGroup(player, group)) {
            return;
        }
        groups.put(group.getId(), group);
        broadcastAddGroup(group);

        if (player == null) {
            return;
        }

        PlayerStateManager manager = getStates();
        manager.setGroup(player, group.getId());

        NetManager.sendToClient(player, new JoinedGroupPacket(group.toClientGroup(), false));
    }

    public void joinGroup(@Nullable Group group, ServerPlayer player, @Nullable String password) {
        if (PluginManager.instance().onJoinGroup(player, group)) {
            return;
        }
        if (group == null) {
            NetManager.sendToClient(player, new JoinedGroupPacket(null, false));
            return;
        }
        if (group.getPassword() != null) {
            if (!group.getPassword().equals(password)) {
                NetManager.sendToClient(player, new JoinedGroupPacket(null, true));
                return;
            }
        }

        PlayerStateManager manager = getStates();
        manager.setGroup(player, group.getId());

        NetManager.sendToClient(player, new JoinedGroupPacket(group.toClientGroup(), false));
    }

    public void leaveGroup(ServerPlayer player) {
        if (PluginManager.instance().onLeaveGroup(player)) {
            return;
        }

        PlayerStateManager manager = getStates();
        manager.setGroup(player, null);
        NetManager.sendToClient(player, new JoinedGroupPacket(null, false));

        cleanupGroups();
    }

    public void cleanupGroups() {
        PlayerStateManager manager = getStates();
        List<UUID> usedGroups = manager.getStates().stream().filter(PlayerState::hasGroup).map(PlayerState::getGroup).distinct().toList();
        List<UUID> groupsToRemove = groups.entrySet().stream().filter(entry -> !entry.getValue().isPersistent()).map(Map.Entry::getKey).filter(uuid -> !usedGroups.contains(uuid)).toList();
        for (UUID uuid : groupsToRemove) {
            removeGroup(uuid);
        }
    }

    public boolean removeGroup(UUID groupId) {
        Group group = groups.get(groupId);
        if (group == null) {
            return false;
        }

        PlayerStateManager manager = getStates();
        if (manager.getStates().stream().anyMatch(state -> state.hasGroup() && state.getGroup().equals(groupId))) {
            return false;
        }

        if (PluginManager.instance().onRemoveGroup(group)) {
            return false;
        }

        groups.remove(groupId);
        broadcastRemoveGroup(groupId);
        // TODO Handle kicking players from group instead of preventing it
        return true;
    }

    @Nullable
    public Group getGroup(UUID groupID) {
        return groups.get(groupID);
    }

    private void broadcastAddGroup(Group group) {
        AddGroupPacket packet = new AddGroupPacket(group.toClientGroup());
        server.getServer().getPlayerList().getPlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    private void broadcastRemoveGroup(UUID group) {
        RemoveGroupPacket packet = new RemoveGroupPacket(group);
        server.getServer().getPlayerList().getPlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    @Nullable
    public Group getPlayerGroup(ServerPlayer player) {
        PlayerState state = server.getPlayerStateManager().getState(player.getUUID());
        if (state == null) {
            return null;
        }
        UUID groupId = state.getGroup();
        if (groupId == null) {
            return null;
        }
        return getGroup(groupId);
    }

    public Map<UUID, Group> getGroups() {
        return groups;
    }
}
