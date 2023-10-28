package de.maxhenkel.voicechat.gui.group;

import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.*;
import java.util.stream.Collectors;

public class JoinGroupList extends ListScreenListBase<JoinGroupEntry> {

    protected final ListScreenBase parent;

    public JoinGroupList(ListScreenBase parent, int width, int height, int x, int y, int size) {
        super(width, height, x, y, size);
        this.parent = parent;
        updateGroups();
    }

    private void updateGroups() {
        Map<UUID, JoinGroupEntry.Group> groups = ClientManager.getGroupManager().getGroups().stream().filter(clientGroup -> !clientGroup.isHidden()).collect(Collectors.toMap(ClientGroup::getId, JoinGroupEntry.Group::new));
        Collection<PlayerState> playerStates = ClientManager.getPlayerStateManager().getPlayerStates(true);

        for (PlayerState state : playerStates) {
            if (!state.hasGroup()) {
                continue;
            }
            JoinGroupEntry.Group group = groups.get(state.getGroup());
            if (group == null) {
                continue;
            }
            group.getMembers().add(state);
        }

        groups.values().forEach(group -> group.getMembers().sort(Comparator.comparing(PlayerState::getName)));

        replaceEntries(groups.values().stream().map(group -> new JoinGroupEntry(parent, group)).sorted(Comparator.comparing(o -> o.getGroup().getGroup().getName())).collect(Collectors.toList()));
    }

    public static void update() {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof JoinGroupScreen) {
            JoinGroupScreen joinGroupScreen = (JoinGroupScreen) screen;
            if (joinGroupScreen.groupList != null) {
                joinGroupScreen.groupList.updateGroups();
            }
        }
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }
}
