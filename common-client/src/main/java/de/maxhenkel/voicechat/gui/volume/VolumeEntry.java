package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public abstract class VolumeEntry extends ListScreenEntryBase<VolumeEntry> {

    protected static final Component OTHER_VOLUME = Component.translatable("message.voicechat.other_volume");
    protected static final Component OTHER_VOLUME_DESCRIPTION = Component.translatable("message.voicechat.other_volume.description");
    protected static final Identifier OTHER_VOLUME_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/other_volume");

    protected static final int SKIN_SIZE = 24;
    protected static final int PADDING = 4;
    protected static final int SLIDER_WIDTH = 90;
    protected static final int ARROW_SIZE = 6;
    protected static final int ARROW_SPACING = 1;
    protected static final int ARROW_COLUMN_WIDTH = ARROW_SPACING + ARROW_SIZE + ARROW_SPACING;
    protected static final int BG_FILL = ARGB.color(255, 74, 74, 74);

    protected final Minecraft minecraft;
    protected final AdjustVolumesScreen screen;
    protected final AdjustVolumeSlider volumeSlider;
    protected final boolean indented;
    protected final boolean offset;

    public VolumeEntry(AdjustVolumesScreen screen, AdjustVolumeSlider.AdjustVolumeEntry entry, boolean indented, boolean offset) {
        this.minecraft = Minecraft.getInstance();
        this.screen = screen;
        this.indented = indented;
        this.offset = offset;
        this.volumeSlider = new AdjustVolumeSlider(0, 0, SLIDER_WIDTH, 20, entry);
        this.children.add(volumeSlider);
    }

    @Override
    public void extractContent(GuiGraphicsExtractor guiGraphics, boolean hovered) {
        int indent = indented ? ARROW_COLUMN_WIDTH - PADDING : 0;
        int left = getContentX() + indent;
        int top = getContentY();
        int width = getContentWidth() - indent;
        int height = getContentHeight();
        int skinX = left + (offset ? ARROW_COLUMN_WIDTH : PADDING);
        int skinY = top + (height - SKIN_SIZE) / 2;
        int textX = skinX + SKIN_SIZE + PADDING;
        int textY = top + (height - minecraft.font.lineHeight) / 2;

        guiGraphics.fill(left, top, left + width, top + height, BG_FILL);

        volumeSlider.setPosition(left + width - volumeSlider.getWidth() - PADDING, top + (height - volumeSlider.getHeight()) / 2);

        renderElement(guiGraphics, top, left, width, height, hovered, skinX, skinY, textX, textY);

        volumeSlider.extractRenderState(guiGraphics);
    }

    public abstract void renderElement(GuiGraphicsExtractor guiGraphics, int top, int left, int width, int height, boolean hovered, int skinX, int skinY, int textX, int textY);

    protected void renderScrollingString(GuiGraphicsExtractor guiGraphics, Component text, int textX, int textY) {
        int textSpace = volumeSlider.getX() - PADDING - textX;
        int textWidth = minecraft.font.width(text);
        if (textWidth > textSpace) {
            guiGraphics.textRenderer().acceptScrollingWithDefaultCenter(text, textX, textX + textSpace, textY, textY + minecraft.font.lineHeight);
        } else {
            guiGraphics.text(minecraft.font, text, textX, textY, 0xFFFFFFFF, false);
        }
    }

}
