package de.maxhenkel.voicechat.gui.volume;

import com.google.common.collect.Lists;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.Util;

import java.util.*;

public class AdjustVolumeList extends ListScreenListBase<PlayerVolumeEntry> {

    protected PlayerVolumesScreen screen;
    protected final List<PlayerVolumeEntry> players;
    protected String filter;

    public AdjustVolumeList(int width, int height, int x, int y, int size, PlayerVolumesScreen screen) {
        super(width, height, x, y, size);
        this.screen = screen;
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

    public void updatePlayerList(Collection<PlayerState> onlinePlayers) {
        players.clear();
        for (PlayerState state : onlinePlayers) {
            players.add(new PlayerVolumeEntry(state, screen));
        }

        if (VoicechatClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment.get()) {
            addOfflinePlayers(onlinePlayers);
        }

        updateFilter();
    }

    private void addOfflinePlayers(Collection<PlayerState> onlinePlayers) {
        for (UUID uuid : VoicechatClient.VOLUME_CONFIG.getVolumes().keySet()) {
            if (uuid.equals(Util.NIL_UUID)) {
                continue;
            }
            if (onlinePlayers.stream().anyMatch(state -> uuid.equals(state.getUuid()))) {
                continue;
            }

            String name = VoicechatClient.USERNAME_CACHE.getUsername(uuid);

            if (name == null) {
                continue;
            }

            players.add(new PlayerVolumeEntry(new PlayerState(uuid, name, false, true), screen));
        }
    }

    public void updateFilter() {
        clearEntries();
        List<PlayerVolumeEntry> filteredPlayers = new ArrayList<>(players);
        if (!filter.isEmpty()) {
            filteredPlayers.removeIf(playerEntry -> playerEntry.getState() == null || !playerEntry.getState().getName().toLowerCase(Locale.ROOT).contains(filter));
        }
        filteredPlayers.sort((e1, e2) -> e1.getState().getName().compareToIgnoreCase(e2.getState().getName()));
        if (filter.isEmpty()) {
            filteredPlayers.add(0, new PlayerVolumeEntry(null, screen));
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
