package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public abstract class ListScreenListBase<T extends ListScreenEntryBase<T>> extends ContainerObjectSelectionList<T> {

    public ListScreenListBase(int width, int height, int top, int size) {
        super(Minecraft.getInstance(), width, height, top, top + height, size);
    }

    public void updateSize(int width, int height, int top) {
        updateSize(width, height, top, top + height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        double scale = minecraft.getWindow().getGuiScale();
        int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
        RenderSystem.enableScissor(0, (int) ((double) (scaledHeight - y1) * scale), Integer.MAX_VALUE / 2, (int) ((double) height * scale));
        super.render(guiGraphics, x, y, partialTicks);
        RenderSystem.disableScissor();
    }

}
