package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.JoinedGroupPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.server.level.ServerPlayer;

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
        CommonCompatibilityManager.INSTANCE.getNetManager().joinGroupChannel.registerServerListener((server, player, handler, packet) -> {
            if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
                return;
            }
            joinGroup(groups.get(packet.getGroup()), player, packet.getPassword());
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().createGroupChannel.registerServerListener((server, player, handler, packet) -> {
            if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
                return;
            }
            Group group = new Group(UUID.randomUUID(), packet.getName(), packet.getPassword());
            addGroup(group, player);
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().leaveGroupChannel.registerServerListener((server, player, handler, packet) -> {
            leaveGroup(player);
        });
    }

    private PlayerStateManager getStates() {
        return Voicechat.SERVER.getServer().getPlayerStateManager();
    }

    public boolean addGroup(Group group, ServerPlayer player) {
        groups.put(group.getId(), group);

        PlayerStateManager manager = getStates();
        PlayerState state = manager.getState(player.getUUID());
        if (state == null) {
            state = PlayerStateManager.defaultDisconnectedState(player);
        }
        state.setGroup(group.toClientGroup());
        manager.setState(player.server, player.getUUID(), state);
        CommonCompatibilityManager.INSTANCE.getNetManager().sendToClient(player, new JoinedGroupPacket(group.toClientGroup()));
        return true;
    }

    public boolean joinGroup(@Nullable Group group, ServerPlayer player, String password) {
        if (group == null) {
            CommonCompatibilityManager.INSTANCE.getNetManager().sendToClient(player, new JoinedGroupPacket(null));
            return false;
        }
        if (group.getPassword() != null) {
            if (!group.getPassword().equals(password)) {
                CommonCompatibilityManager.INSTANCE.getNetManager().sendToClient(player, new JoinedGroupPacket(null));
                return false;
            }
        }
        PlayerStateManager manager = getStates();
        PlayerState state = manager.getState(player.getUUID());
        if (state == null) {
            state = PlayerStateManager.defaultDisconnectedState(player);
        }
        state.setGroup(group.toClientGroup());
        manager.setState(player.server, player.getUUID(), state);
        CommonCompatibilityManager.INSTANCE.getNetManager().sendToClient(player, new JoinedGroupPacket(group.toClientGroup()));
        return true;
    }

    public void leaveGroup(ServerPlayer player) {
        PlayerStateManager manager = getStates();
        PlayerState state = manager.getState(player.getUUID());
        if (state != null) {
            state.setGroup(null);
            manager.setState(player.server, player.getUUID(), state);
        }
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
