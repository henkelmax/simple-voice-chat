package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

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
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        double scale = scaledResolution.getScaleFactor();
        enableScissor((int) ((double) getRowLeft() * scale), (int) ((double) (height - bottom) * scale), (int) ((double) (getScrollBarX() + 6) * scale), (int) ((double) (height - (height - bottom + 4) - top + 4) * scale));
        super.drawScreen(mouseXIn, mouseYIn, partialTicks);
        disableScissor();
    }

    public static void enableScissor(int x1, int y1, int x2, int y2) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x1, y1, x2, y2);
    }

    public static void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public int getRowLeft() {
        return left + width / 2 - getListWidth() / 2 + 2;
    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {

    }

    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {

    }
}
