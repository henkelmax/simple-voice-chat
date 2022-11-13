package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ListScreenListBase<T extends ListScreenEntryBase> extends GuiListExtended {

    private final List<T> entries;

    public ListScreenListBase(int width, int height, int x, int y, int size) {
        super(Minecraft.getMinecraft(), width, height, x, y, size);
        entries = new ArrayList<>();
    }

    public List<T> children() {
        return entries;
    }

    public void replaceEntries(Collection<T> e) {
        entries.clear();
        entries.addAll(e);
    }

    public void clearEntries() {
        entries.clear();
    }

    public T getEntry(int index) {
        return entries.get(index);
    }

    public void removeEntry(T entry) {
        entries.remove(entry);
    }

    public void addEntry(T entry) {
        entries.add(entry);
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return entries.get(index);
    }

    @Override
    protected int getSize() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public void updateSize(int width, int height, int y, int x) {
        this.width = width;
        this.height = height;
        this.top = y;
        this.bottom = y;
        this.left = 0;
        this.right = width;
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        //TODO Fix rendering scissor
        super.drawScreen(x, y, partialTicks);
        /*ScaledResolution scaledResolution = new ScaledResolution(mc);
        double scale = scaledResolution.getScaleFactor();
        RenderSystem.enableScissor((int) ((double) getRowLeft() * scale), (int) ((double) (height - y1) * scale), (int) ((double) (getScrollbarPosition() + 6) * scale), (int) ((double) (height - (height - y1) - y0 - 4) * scale));
        super.render(poseStack, x, y, partialTicks);
        RenderSystem.disableScissor();*/
    }

}
