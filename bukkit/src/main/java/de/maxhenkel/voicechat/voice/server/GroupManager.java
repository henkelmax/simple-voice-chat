package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.*;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO Rename to ServerGroupManager
public class GroupManager {

    private final Map<UUID, Group> groups;

    public GroupManager() {
        groups = new ConcurrentHashMap<>();
    }

    public void onJoinGroupPacket(Player player, JoinGroupPacket packet) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            return;
        }
        if (!player.hasPermission(PermissionManager.GROUPS_PERMISSION)) {
            NetManager.sendStatusMessage(player, Component.translatable("message.voicechat.no_group_permission"));
            return;
        }
        joinGroup(groups.get(packet.getGroup()), player, packet.getPassword());
    }

    public void onCreateGroupPacket(Player player, CreateGroupPacket packet) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            return;
        }
        if (!player.hasPermission(PermissionManager.GROUPS_PERMISSION)) {
            NetManager.sendStatusMessage(player, Component.translatable("message.voicechat.no_group_permission"));
            return;
        }
        Group group = new Group(UUID.randomUUID(), packet.getName(), packet.getPassword());
        addGroup(group, player);
    }

    public void onLeaveGroupPacket(Player player, LeaveGroupPacket packet) {
        leaveGroup(player);
    }

    public void onPlayerCompatibilityCheckSucceeded(Player player) {
        Voicechat.logDebug("Synchronizing {} groups with {}", groups.size(), player.getDisplayName());
        for (Group category : groups.values()) {
            broadcastAddGroup(category);
        }
    }

    private PlayerStateManager getStates() {
        return Voicechat.SERVER.getServer().getPlayerStateManager();
    }

    public void addGroup(Group group, @Nullable Player player) {
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

    public void joinGroup(@Nullable Group group, Player player, String password) {
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

    public void leaveGroup(Player player) {
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
        Voicechat.INSTANCE.getServer().getOnlinePlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    private void broadcastRemoveGroup(UUID group) {
        RemoveGroupPacket packet = new RemoveGroupPacket(group);
        Voicechat.INSTANCE.getServer().getOnlinePlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    public Map<UUID, Group> getGroups() {
        return groups;
    }

}