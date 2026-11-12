package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdjustVolumeList extends ListScreenListBase<VolumeEntry> {

    // Remember expanded categories and the selected tab for the whole session
    private static final Set<String> EXPANDED_CATEGORIES = new HashSet<>();
    private static boolean showPlayers = true;

    protected AdjustVolumesScreen screen;
    protected String filter;

    public AdjustVolumeList(int width, int height, int top, int itemSize, AdjustVolumesScreen screen) {
        super(width, height, top, itemSize);
        this.screen = screen;
        this.filter = "";
        updateEntryList();
    }

    public static void update() {
        if (Minecraft.getInstance().screen instanceof AdjustVolumesScreen volumesScreen) {
            volumesScreen.volumeList.updateEntryList();
        }
    }

    public void updateEntryList() {
        replaceEntries(showPlayers ? createPlayerEntries() : createOtherEntries());
        refreshScrollAmount();
    }

    private List<VolumeEntry> createPlayerEntries() {
        Collection<PlayerState> onlinePlayers = ClientManager.getPlayerStateManager().getPlayerStates(false);
        List<PlayerState> players = new ArrayList<>(onlinePlayers);
        if (VoicechatClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment.get()) {
            players.addAll(getOfflinePlayers(onlinePlayers));
        }
        return players.stream()
                .filter(player -> matchesFilter(player.getName()))
                .sorted(Comparator.comparing(PlayerState::getName, String.CASE_INSENSITIVE_ORDER))
                .<VolumeEntry>map(player -> new PlayerVolumeEntry(player, screen))
                .toList();
    }

    private List<PlayerState> getOfflinePlayers(Collection<PlayerState> onlinePlayers) {
        List<PlayerState> offlinePlayers = new ArrayList<>();
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

            offlinePlayers.add(new PlayerState(uuid, name, false, false, true));
        }
        return offlinePlayers;
    }

    private List<VolumeEntry> createOtherEntries() {
        List<VolumeEntry> entries = new ArrayList<>();
        if (filter.isEmpty()) {
            entries.add(new OtherVolumeEntry(screen));
        }
        getCategoryTree().forEach((category, children) -> {
            if (filter.isEmpty()) {
                addExpandableCategory(entries, category, children);
            } else {
                addMatchingCategory(entries, category, children);
            }
        });
        return entries;
    }

    private Map<VolumeCategoryImpl, List<VolumeCategoryImpl>> getCategoryTree() {
        Map<String, VolumeCategoryImpl> categoriesById = ClientManager.getCategoryManager().getCategories().stream()
                .collect(Collectors.toMap(VolumeCategoryImpl::getId, Function.identity()));
        List<VolumeCategoryImpl> sortedCategories = categoriesById.values().stream()
                .sorted(Comparator.comparing(VolumeCategoryImpl::getSearchName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<VolumeCategoryImpl, List<VolumeCategoryImpl>> tree = new LinkedHashMap<>();
        for (VolumeCategoryImpl category : sortedCategories) {
            if (getValidParent(category, categoriesById) == null) {
                tree.put(category, new ArrayList<>());
            }
        }
        for (VolumeCategoryImpl category : sortedCategories) {
            VolumeCategoryImpl parent = getValidParent(category, categoriesById);
            if (parent != null) {
                tree.get(parent).add(category);
            }
        }
        return tree;
    }

    @Nullable
    private static VolumeCategoryImpl getValidParent(VolumeCategoryImpl category, Map<String, VolumeCategoryImpl> categoriesById) {
        VolumeCategoryImpl parent = categoriesById.get(category.getParent());
        if (parent == null || parent.getParent() != null) {
            return null;
        }
        return parent;
    }

    private void addExpandableCategory(List<VolumeEntry> entries, VolumeCategoryImpl category, List<VolumeCategoryImpl> children) {
        if (children.isEmpty()) {
            entries.add(new CategoryVolumeEntry(category, screen, false, true));
            return;
        }
        boolean expanded = EXPANDED_CATEGORIES.contains(category.getId());
        entries.add(new ExpandableCategoryVolumeEntry(category, screen, expanded, () -> toggleExpanded(category)));
        if (expanded) {
            addChildren(entries, children);
        }
    }

    private void addMatchingCategory(List<VolumeEntry> entries, VolumeCategoryImpl category, List<VolumeCategoryImpl> children) {
        List<VolumeCategoryImpl> matchingChildren = children.stream().filter(child -> matchesFilter(child.getSearchName())).toList();
        if (matchesFilter(category.getSearchName()) || !matchingChildren.isEmpty()) {
            entries.add(new CategoryVolumeEntry(category, screen, false, true));
            addChildren(entries, matchingChildren);
        }
    }

    private void addChildren(List<VolumeEntry> entries, List<VolumeCategoryImpl> children) {
        for (VolumeCategoryImpl child : children) {
            entries.add(new CategoryVolumeEntry(child, screen, true, false));
        }
    }

    private void toggleExpanded(VolumeCategoryImpl category) {
        if (!EXPANDED_CATEGORIES.remove(category.getId())) {
            EXPANDED_CATEGORIES.add(category.getId());
        }
        updateEntryList();
    }

    private boolean matchesFilter(String name) {
        return name.toLowerCase(Locale.ROOT).contains(filter);
    }

    public void setFilter(String filter) {
        this.filter = filter;
        updateEntryList();
        setScrollAmount(0D);
    }

    public void setShowPlayers(boolean showPlayers) {
        AdjustVolumeList.showPlayers = showPlayers;
        updateEntryList();
        setScrollAmount(0D);
    }

    public boolean isShowingPlayers() {
        return showPlayers;
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

    @Override
    public int getRowLeft() {
        return screen.getGuiLeft() + 6;
    }

    @Override
    public int getRowWidth() {
        return 224;
    }

}
