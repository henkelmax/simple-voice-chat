package de.maxhenkel.voicechat.gui.volume;

import com.google.common.collect.Lists;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.util.*;

public class AdjustVolumeList extends ListScreenListBase<VolumeEntry> {

    protected AdjustVolumesScreen screen;
    protected final List<VolumeEntry> entries;
    protected String filter;
    protected VolumeTab tab;

    public AdjustVolumeList(int width, int height, int top, int size, AdjustVolumesScreen screen, VolumeTab tab) {
        super(width, height, top, size);
        this.screen = screen;
        this.entries = Lists.newArrayList();
        this.filter = "";
        this.tab = tab;
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        updateEntryList();
    }

    public static void update() {
        if (Minecraft.getInstance().screen instanceof AdjustVolumesScreen volumesScreen) {
            volumesScreen.volumeList.updateEntryList();
        }
    }

    public void updateEntryList() {
        entries.clear();
        entries.addAll(tab.createEntries(screen));
        updateFilter();
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

    public void setTab(VolumeTab tab) {
        this.tab = tab;
        updateEntryList();
        setScrollAmount(0D);
    }

    public VolumeTab getTab() {
        return tab;
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}
