package de.maxhenkel.voicechat.gui.volume;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class AdjustVolumeList extends ContainerObjectSelectionList<PlayerVolumeEntry> {

    private final List<PlayerVolumeEntry> players;
    private final List<PlayerVolumeEntry> filteredPlayers;
    private String filter;

    public AdjustVolumeList(int width, int height, int x, int y, int size) {
        super(Minecraft.getInstance(), width, height, x, y, size);
        this.players = Lists.newArrayList();
        this.filteredPlayers = Lists.newArrayList();
        this.filter = "";
        setRenderBackground(false);
        setRenderTopAndBottom(false);
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, float partialTicks) {
        double scale = minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) ((double) getRowLeft() * scale), (int) ((double) (height - y1) * scale), (int) ((double) (getScrollbarPosition() + 6) * scale), (int) ((double) (height - (height - y1) - y0 - 4) * scale));
        super.render(poseStack, x, y, partialTicks);
        RenderSystem.disableScissor();
    }

    public void updatePlayerList(Collection<PlayerState> collection) {
        players.clear();
        for (PlayerState state : collection) {
            players.add(new PlayerVolumeEntry(state));
        }
        updateFilter();
    }

    public void updateFilter() {
        filteredPlayers.clear();
        filteredPlayers.addAll(players);
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
        return filteredPlayers.isEmpty();
    }

}
