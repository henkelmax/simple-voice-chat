package de.maxhenkel.voicechat.gui.group;

import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class GroupList extends ListScreenListBase<GroupEntry> {

    protected final ListScreenBase parent;

    public GroupList(ListScreenBase parent, int width, int height, int x, int y, int size) {
        super(width, height, x, y, size);
        this.parent = parent;
        tick();
    }

    public void tick() {
        List<PlayerState> playerStates = ClientManager.getPlayerStateManager().getPlayerStates(true);
        ClientGroup group = ClientManager.getPlayerStateManager().getGroup();
        if (group == null) {
            clearEntries();
            mc.displayGuiScreen(null);
            return;
        }
        boolean changed = false;
        List<GroupEntry> toRemove = new LinkedList<>();
        for (GroupEntry entry : children()) {
            PlayerState state = ClientManager.getPlayerStateManager().getState(entry.getState().getUuid());
            if (state == null) {
                toRemove.add(entry);
                changed = true;
                continue;
            }
            entry.setState(state);
            if (!isInGroup(state, group)) {
                toRemove.add(entry);
                changed = true;
            }
        }
        for (GroupEntry entry : toRemove) {
            removeEntry(entry);
        }
        for (PlayerState state : playerStates) {
            if (isInGroup(state, group)) {
                if (children().stream().noneMatch(groupEntry -> groupEntry.getState().getUuid().equals(state.getUuid()))) {
                    addEntry(new GroupEntry(parent, state));
                    changed = true;
                }
            }
        }

        if (changed) {
            children().sort(Comparator.comparing(o -> o.getState().getName()));
        }
    }

    private boolean isInGroup(PlayerState state, ClientGroup group) {
        return state.hasGroup() && state.getGroup().getId().equals(group.getId());
    }

}
