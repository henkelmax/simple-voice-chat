package de.maxhenkel.voicechat.gui.volume;

import com.google.common.collect.Lists;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
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
        updateEntryList();
    }

    public static void update() {
        if (Minecraft.getMinecraft().currentScreen instanceof AdjustVolumesScreen) {
            ((AdjustVolumesScreen) Minecraft.getMinecraft().currentScreen).volumeList.updateEntryList();
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
                if (volumeEntry instanceof PlayerVolumeEntry) {
                    PlayerVolumeEntry playerVolumeEntry = (PlayerVolumeEntry) volumeEntry;
                    return playerVolumeEntry.getState() == null || !playerVolumeEntry.getState().getName().toLowerCase(Locale.ROOT).contains(filter);
                } else if (volumeEntry instanceof CategoryVolumeEntry) {
                    CategoryVolumeEntry categoryVolumeEntry = (CategoryVolumeEntry) volumeEntry;
                    return !categoryVolumeEntry.getCategory().getSearchName().toLowerCase(Locale.ROOT).contains(filter);
                }
                return true;
            });
        }
        filteredEntries.sort((e1, e2) -> volumeEntryToSearchString(e1).compareToIgnoreCase(volumeEntryToSearchString(e2)));
        replaceEntries(filteredEntries);
    }

    private String volumeEntryToSearchString(VolumeEntry entry) {
        if (entry instanceof PlayerVolumeEntry) {
            PlayerVolumeEntry playerVolumeEntry = (PlayerVolumeEntry) entry;
            return playerVolumeEntry.getState() == null ? "" : playerVolumeEntry.getState().getName();
        } else if (entry instanceof CategoryVolumeEntry) {
            CategoryVolumeEntry categoryVolumeEntry = (CategoryVolumeEntry) entry;
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
        amountScrolled = 0F;
    }

    public VolumeTab getTab() {
        return tab;
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}
