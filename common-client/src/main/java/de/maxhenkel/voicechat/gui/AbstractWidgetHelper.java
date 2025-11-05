package de.maxhenkel.voicechat.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class AbstractWidgetHelper extends AbstractWidget {

    private AbstractWidgetHelper(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public static void renderScrollingText(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
        renderScrollingString(guiGraphics, font, text, minX, minY, maxX, maxY, color);
    }

}
