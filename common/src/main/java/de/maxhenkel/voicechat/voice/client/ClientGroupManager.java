package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.group.JoinGroupList;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientGroupManager {

    private Map<UUID, ClientGroup> groups;

    public ClientGroupManager() {
        groups = new ConcurrentHashMap<>();
        CommonCompatibilityManager.INSTANCE.getNetManager().addGroupChannel.setClientListener((client, handler, packet) -> {
            groups.put(packet.getGroup().getId(), packet.getGroup());
            Voicechat.logDebug("Added group '{}' ({})", packet.getGroup().getName(), packet.getGroup().getId());
            JoinGroupList.update();
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().removeGroupChannel.setClientListener((client, handler, packet) -> {
            groups.remove(packet.getGroupId());
            Voicechat.logDebug("Removed group {}", packet.getGroupId());
            JoinGroupList.update();
        });
        ClientCompatibilityManager.INSTANCE.onDisconnect(() -> groups.clear());
    }

    @Nullable
    public ClientGroup getGroup(UUID id) {
        return groups.get(id);
    }

    public Collection<ClientGroup> getGroups() {
        return groups.values();
    }
}
