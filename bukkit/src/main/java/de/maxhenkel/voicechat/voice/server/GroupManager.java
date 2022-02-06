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
            //TODO change to status bar message
            NetManager.sendMessage(player, Component.translatable("message.voicechat.no_group_permission"));
            return;
        }
        joinGroup(groups.get(packet.getGroup()), player, packet.getPassword());
    }

    public void onCreateGroupPacket(Player player, CreateGroupPacket packet) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            return;
        }
        if (!player.hasPermission(PermissionManager.GROUPS_PERMISSION)) {
            //TODO change to status bar message
            NetManager.sendMessage(player, Component.translatable("message.voicechat.no_group_permission"));
            return;
        }
        Group group = new Group(UUID.randomUUID(), packet.getName(), packet.getPassword());
        addGroup(group, player);
    }

    public void onLeaveGroupPacket(Player player, LeaveGroupPacket packet) {
        leaveGroup(player);
    }

    private PlayerStateManager getStates() {
        return Voicechat.SERVER.getServer().getPlayerStateManager();
    }

    public void addGroup(Group group, Player player) {
        if (PluginManager.instance().onCreateGroup(player, group)) {
            return;
        }
        groups.put(group.getId(), group);

        PlayerStateManager manager = getStates();
        manager.setGroup(player, group.toClientGroup());

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
        manager.setGroup(player, group.toClientGroup());

        NetManager.sendToClient(player, new JoinedGroupPacket(group.toClientGroup(), false));
    }

    public void leaveGroup(Player player) {
        if (PluginManager.instance().onLeaveGroup(player)) {
            return;
        }

        PlayerStateManager manager = getStates();
        manager.setGroup(player, null);
        NetManager.sendToClient(player, new JoinedGroupPacket(null, false));

        cleanEmptyGroups();
    }

    public void cleanEmptyGroups() {
        PlayerStateManager manager = getStates();
        List<UUID> usedGroups = manager.getStates().stream().filter(PlayerState::hasGroup).map(state -> state.getGroup().getId()).distinct().toList();
        List<UUID> groupsToRemove = groups.keySet().stream().filter(uuid -> !usedGroups.contains(uuid)).toList();
        for (UUID uuid : groupsToRemove) {
            groups.remove(uuid);
        }
    }

    @Nullable
    public Group getGroup(UUID groupID) {
        return groups.get(groupID);
    }

}