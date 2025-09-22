package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public abstract class ListScreenListBase<T extends ListScreenEntryBase<T>> extends ContainerObjectSelectionList<T> {

    public ListScreenListBase(int width, int height, int top, int itemSize) {
        super(Minecraft.getInstance(), width, height, top, itemSize);
    }

    public void updateSize(int width, int height, int x, int y){
        setRectangle(width, height, x, y);
        refreshScrollAmount();
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
    }

}
