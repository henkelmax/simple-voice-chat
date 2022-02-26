package de.maxhenkel.voicechat.gui.volume;

import com.google.common.collect.Lists;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class AdjustVolumeList extends ListScreenListBase<PlayerVolumeEntry> {

    protected final List<PlayerVolumeEntry> players;
    protected String filter;

    public AdjustVolumeList(int width, int height, int x, int y, int size) {
        super(width, height, x, y, size);
        this.players = Lists.newArrayList();
        this.filter = "";
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        updatePlayerList(ClientManager.getPlayerStateManager().getPlayerStates(false));
    }

    public void tick() {
        List<PlayerState> playerStates = ClientManager.getPlayerStateManager().getPlayerStates(false);
        if (hasChanged(playerStates)) {
            updatePlayerList(playerStates);
        }
    }

    private boolean hasChanged(List<PlayerState> playerStates) {
        for (PlayerState state : playerStates) {
            boolean match = players.stream().anyMatch(entry -> entry.getState().getUuid().equals(state.getUuid()));
            if (!match) {
                return true;
            }
        }
        return false;
    }

    public void updatePlayerList(Collection<PlayerState> collection) {
        players.clear();
        for (PlayerState state : collection) {
            players.add(new PlayerVolumeEntry(state));
        }
        updateFilter();
    }

    public void updateFilter() {
        clearEntries();
        List<PlayerVolumeEntry> filteredPlayers = new ArrayList<>(players);
        if (!filter.isEmpty()) {
            filteredPlayers.removeIf(playerEntry -> playerEntry.getState() == null || !playerEntry.getState().getName().toLowerCase(Locale.ROOT).contains(filter));
        }
        filteredPlayers.sort((e1, e2) -> e1.getState().getName().compareToIgnoreCase(e2.getState().getName()));
        if (filter.isEmpty()) {
            filteredPlayers.add(0, new PlayerVolumeEntry(null));
        }
        replaceEntries(filteredPlayers);
    }

    public void setFilter(String filter) {
        this.filter = filter;
        updateFilter();
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}
