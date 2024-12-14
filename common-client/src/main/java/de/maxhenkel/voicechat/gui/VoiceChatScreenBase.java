package de.maxhenkel.voicechat.gui;

import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class VoiceChatScreenBase extends ScreenBase {

    public static final int FONT_COLOR = 4210752;

    protected List<HoverArea> hoverAreas;
    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    protected VoiceChatScreenBase(ITextComponent title, int xSize, int ySize) {
        super(title);
        this.xSize = xSize;
        this.ySize = ySize;
        this.hoverAreas = new ArrayList<>();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;
    }

    public int getGuiLeft() {
        return guiLeft;
    }

    public int getGuiTop() {
        return guiTop;
    }

    protected int getFontColor() {
        return isIngame() ? FONT_COLOR : 0xFFFFFF;
    }

    public void drawHoverAreas(int mouseX, int mouseY) {
        for (HoverArea hoverArea : hoverAreas) {
            if (hoverArea.tooltip != null && hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                drawHoveringText(hoverArea.tooltip.get(), mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

    public static class HoverArea {
        private final int posX, posY;
        private final int width, height;
        @Nullable
        private final Supplier<List<String>> tooltip;

        public HoverArea(int posX, int posY, int width, int height) {
            this(posX, posY, width, height, null);
        }

        public HoverArea(int posX, int posY, int width, int height, Supplier<List<String>> tooltip) {
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
        public Supplier<List<String>> getTooltip() {
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
