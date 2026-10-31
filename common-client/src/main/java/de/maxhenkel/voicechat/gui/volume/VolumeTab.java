package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class VolumeTab {

    public static final String PLAYERS = "players";
    public static final String OTHER = "other";

    private final String id;
    private final ITextComponent title;

    private VolumeTab(String id, ITextComponent title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public ITextComponent getTitle() {
        return title;
    }

    public List<VolumeEntry> createEntries(AdjustVolumesScreen screen) {
        List<VolumeEntry> entries = new ArrayList<>();
        if (id.equals(PLAYERS)) {
            addPlayers(entries, screen);
        } else if (id.equals(OTHER)) {
            entries.add(new PlayerVolumeEntry(null, screen));
        }
        for (VolumeCategoryImpl category : ClientManager.getCategoryManager().getCategories()) {
            if (id.equals(getTabId(category))) {
                entries.add(new CategoryVolumeEntry(category, screen));
            }
        }
        return entries;
    }

    private static void addPlayers(List<VolumeEntry> entries, AdjustVolumesScreen screen) {
        Collection<PlayerState> onlinePlayers = ClientManager.getPlayerStateManager().getPlayerStates(false);

        for (PlayerState state : onlinePlayers) {
            entries.add(new PlayerVolumeEntry(state, screen));
        }

        if (VoicechatClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment.get()) {
            addOfflinePlayers(entries, onlinePlayers, screen);
        }
    }

    private static void addOfflinePlayers(List<VolumeEntry> entries, Collection<PlayerState> onlinePlayers, AdjustVolumesScreen screen) {
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

    public static List<VolumeTab> createTabs() {
        List<VolumeTab> tabs = new ArrayList<>();
        tabs.add(new VolumeTab(PLAYERS, new TranslationTextComponent("message.voicechat.volume_tab.players")));
        tabs.add(new VolumeTab(OTHER, new TranslationTextComponent("message.voicechat.volume_tab.other")));

        // Sorting by category ID makes the tab name deterministic if categories define different names for the same tab
        List<VolumeCategoryImpl> categories = new ArrayList<>(ClientManager.getCategoryManager().getCategories());
        categories.sort(Comparator.comparing(VolumeCategoryImpl::getId));
        Set<String> customTabIds = new HashSet<>();
        Map<String, ITextComponent> customTabTitles = new HashMap<>();
        for (VolumeCategoryImpl category : categories) {
            String tabId = getTabId(category);
            if (tabId.equals(PLAYERS) || tabId.equals(OTHER)) {
                continue;
            }
            customTabIds.add(tabId);
            ITextComponent title = category.getTabDisplayName();
            if (title != null) {
                customTabTitles.putIfAbsent(tabId, title);
            }
        }
        customTabIds.stream()
                .map(tabId -> new VolumeTab(tabId, customTabTitles.getOrDefault(tabId, new StringTextComponent(tabId))))
                .sorted(Comparator.comparing(tab -> tab.getTitle().getString(), String.CASE_INSENSITIVE_ORDER))
                .forEach(tabs::add);
        return tabs;
    }

    private static String getTabId(VolumeCategoryImpl category) {
        return category.getTab() != null ? category.getTab() : OTHER;
    }

}
