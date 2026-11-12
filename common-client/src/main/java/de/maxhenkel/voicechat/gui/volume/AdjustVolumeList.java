package de.maxhenkel.voicechat.gui.volume;

import com.google.common.collect.Lists;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

import java.util.*;

public class AdjustVolumeList extends ListScreenListBase<VolumeEntry> {

    protected AdjustVolumesScreen screen;
    protected final List<VolumeEntry> entries;
    protected String filter;
    protected boolean showPlayers;

    public AdjustVolumeList(int width, int height, int top, int itemSize, AdjustVolumesScreen screen) {
        super(width, height, top, itemSize);
        this.screen = screen;
        this.entries = Lists.newArrayList();
        this.filter = "";
        this.showPlayers = true;
        updateEntryList();
    }

    public static void update() {
        if (Minecraft.getInstance().gui.screen() instanceof AdjustVolumesScreen volumesScreen) {
            volumesScreen.volumeList.updateEntryList();
        }
    }

    public void updateEntryList() {
        entries.clear();
        if (showPlayers) {
            addPlayers();
        } else {
            addOther();
        }
        updateFilter();
    }

    private void addPlayers() {
        Collection<PlayerState> onlinePlayers = ClientManager.getPlayerStateManager().getPlayerStates(false);
        for (PlayerState state : onlinePlayers) {
            entries.add(new PlayerVolumeEntry(state, screen));
        }
        if (VoicechatClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment.get()) {
            addOfflinePlayers(onlinePlayers);
        }
    }

    private void addOfflinePlayers(Collection<PlayerState> onlinePlayers) {
        for (UUID uuid : VoicechatClient.PLAYER_VOLUME_CONFIG.getVolumes().keySet()) {
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

            entries.add(new PlayerVolumeEntry(new PlayerState(uuid, name, false, false, true), screen));
        }
    }

    private void addOther() {
        entries.add(new PlayerVolumeEntry(null, screen));
        for (VolumeCategoryImpl category : ClientManager.getCategoryManager().getCategories()) {
            entries.add(new CategoryVolumeEntry(category, screen));
        }
    }

    public void updateFilter() {
        clearEntries();
        List<VolumeEntry> filteredEntries = new ArrayList<>(entries);
        if (!filter.isEmpty()) {
            filteredEntries.removeIf(volumeEntry -> {
                if (volumeEntry instanceof PlayerVolumeEntry playerVolumeEntry) {
                    return playerVolumeEntry.getState() == null || !playerVolumeEntry.getState().getName().toLowerCase(Locale.ROOT).contains(filter);
                } else if (volumeEntry instanceof CategoryVolumeEntry categoryVolumeEntry) {
                    return !categoryVolumeEntry.getCategory().getSearchName().toLowerCase(Locale.ROOT).contains(filter);
                }
                return true;
            });
        }
        filteredEntries.sort((e1, e2) -> volumeEntryToSearchString(e1).compareToIgnoreCase(volumeEntryToSearchString(e2)));
        replaceEntries(filteredEntries);
    }

    private String volumeEntryToSearchString(VolumeEntry entry) {
        if (entry instanceof PlayerVolumeEntry playerVolumeEntry) {
            return playerVolumeEntry.getState() == null ? "" : playerVolumeEntry.getState().getName();
        } else if (entry instanceof CategoryVolumeEntry categoryVolumeEntry) {
            return categoryVolumeEntry.getCategory().getSearchName();
        }
        return "";
    }

    public void setFilter(String filter) {
        this.filter = filter;
        updateFilter();
    }

    public void setShowPlayers(boolean showPlayers) {
        this.showPlayers = showPlayers;
        updateEntryList();
        setScrollAmount(0D);
    }

    public boolean isShowingPlayers() {
        return showPlayers;
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}
