package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.*;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GroupManager {

    private final Map<UUID, Group> groups;

    public GroupManager() {
        groups = new ConcurrentHashMap<>();
    }

    public void onJoinGroupPacket(Player player, JoinGroupPacket packet) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            return;
        }
        joinGroup(groups.get(packet.getGroup()), player, packet.getPassword());
    }

    public void onCreateGroupPacket(Player player, CreateGroupPacket packet) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
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

    public boolean addGroup(Group group, Player player) {
        groups.put(group.getId(), group);

        PlayerStateManager manager = getStates();
        manager.setGroup(player, group.toClientGroup());

        NetManager.sendToClient(player, new JoinedGroupPacket(group.toClientGroup()));
        return true;
    }

    public boolean joinGroup(@Nullable Group group, Player player, String password) {
        if (group == null) {
            NetManager.sendToClient(player, new JoinedGroupPacket(null));
            return false;
        }
        if (group.getPassword() != null) {
            if (!group.getPassword().equals(password)) {
                NetManager.sendToClient(player, new JoinedGroupPacket(null));
                return false;
            }
        }
        PlayerStateManager manager = getStates();
        manager.setGroup(player, group.toClientGroup());

        NetManager.sendToClient(player, new JoinedGroupPacket(group.toClientGroup()));
        return true;
    }

    public void leaveGroup(Player player) {
        PlayerStateManager manager = getStates();
        manager.setGroup(player, null);

        cleanEmptyGroups();
    }

    public void cleanEmptyGroups() {
        PlayerStateManager manager = getStates();
        List<UUID> usedGroups = manager.getStates().stream().filter(PlayerState::hasGroup).map(state -> state.getGroup().getId()).distinct().collect(Collectors.toList());
        List<UUID> groupsToRemove = groups.keySet().stream().filter(uuid -> !usedGroups.contains(uuid)).collect(Collectors.toList());
        for (UUID uuid : groupsToRemove) {
            groups.remove(uuid);
        }
    }

    @Nullable
    public Group getGroup(UUID groupID) {
        return groups.get(groupID);
    }

}