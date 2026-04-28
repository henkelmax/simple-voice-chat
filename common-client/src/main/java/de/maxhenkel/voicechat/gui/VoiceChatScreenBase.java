package de.maxhenkel.voicechat.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class VoiceChatScreenBase extends Screen {

    public static final int FONT_COLOR = -12566464;

    protected List<HoverArea> hoverAreas;
    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    protected VoiceChatScreenBase(Component title, int xSize, int ySize) {
        super(title);
        this.xSize = xSize;
        this.ySize = ySize;
        this.hoverAreas = new ArrayList<>();
    }

    @Override
    protected void init() {
        clearWidgets();
        super.init();

        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        extractBackgroundRenderState(guiGraphics, mouseX, mouseY, delta);
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        extractForegroundRenderState(guiGraphics, mouseX, mouseY, delta);
    }

    public void extractBackgroundRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {

    }

    public void extractForegroundRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {

    }

    public int getGuiLeft() {
        return guiLeft;
    }

    public int getGuiTop() {
        return guiTop;
    }

    protected boolean isIngame() {
        return minecraft.level != null;
    }

    protected int getFontColor() {
        return isIngame() ? FONT_COLOR : TextColor.WHITE.getValue();
    }

    public void drawHoverAreas(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        for (HoverArea hoverArea : hoverAreas) {
            if (hoverArea.tooltip != null && hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                guiGraphics.setTooltipForNextFrame(minecraft.font, hoverArea.tooltip.get(), mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

    public static class HoverArea {
        private final int posX, posY;
        private final int width, height;
        @Nullable
        private final Supplier<List<FormattedCharSequence>> tooltip;

        public HoverArea(int posX, int posY, int width, int height) {
            this(posX, posY, width, height, null);
        }

        public HoverArea(int posX, int posY, int width, int height, Supplier<List<FormattedCharSequence>> tooltip) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.tooltip = tooltip;
        }

        public int getPosX() {
            return posX;
        }

        public int getPosY() {
            return posY;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @Nullable
        public Supplier<List<FormattedCharSequence>> getTooltip() {
            return tooltip;
        }

        public boolean isHovered(int guiLeft, int guiTop, int mouseX, int mouseY) {
            if (mouseX >= guiLeft + posX && mouseX < guiLeft + posX + width) {
                if (mouseY >= guiTop + posY && mouseY < guiTop + posY + height) {
                    return true;
                }
            }
            return false;
        }
    }

}
