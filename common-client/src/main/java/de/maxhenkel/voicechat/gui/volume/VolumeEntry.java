package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public abstract class VolumeEntry extends ListScreenEntryBase<VolumeEntry> {

    protected static final Component OTHER_VOLUME = Component.translatable("message.voicechat.other_volume");
    protected static final Component OTHER_VOLUME_DESCRIPTION = Component.translatable("message.voicechat.other_volume.description");
    protected static final Identifier OTHER_VOLUME_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/other_volume.png");

    protected static final int SKIN_SIZE = 24;
    protected static final int PADDING = 4;
    protected static final int SLIDER_WIDTH = 100;
    protected static final int BG_FILL = ARGB.color(255, 74, 74, 74);

    protected final Minecraft minecraft;
    protected final AdjustVolumesScreen screen;
    protected final AdjustVolumeSlider volumeSlider;

    public VolumeEntry(AdjustVolumesScreen screen, AdjustVolumeSlider.AdjustVolumeEntry entry) {
        this.minecraft = Minecraft.getInstance();
        this.screen = screen;
        this.volumeSlider = new AdjustVolumeSlider(0, 0, SLIDER_WIDTH, 20, entry);
        this.children.add(volumeSlider);
    }

    @Override
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
        int left = getContentX();
        int top = getContentY();
        int width = getContentWidth();
        int height = getContentHeight();
        int skinX = left + PADDING;
        int skinY = top + (height - SKIN_SIZE) / 2;
        int textX = skinX + SKIN_SIZE + PADDING;
        int textY = top + (height - minecraft.font.lineHeight) / 2;

        guiGraphics.fill(left, top, left + width, top + height, BG_FILL);

        renderElement(guiGraphics, top, left, width, height, mouseX, mouseY, hovered, delta, skinX, skinY, textX, textY);

        volumeSlider.setPosition(left + (width - volumeSlider.getWidth() - PADDING), top + (height - volumeSlider.getHeight()) / 2);
        volumeSlider.render(guiGraphics, mouseX, mouseY, delta);
    }

    public abstract void renderElement(GuiGraphics guiGraphics, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY);

    protected void renderScrollingString(GuiGraphics guiGraphics, Component text) {
        int textX = getContentX() + PADDING + SKIN_SIZE + PADDING;
        int textY = getContentY() + (getContentHeight() - minecraft.font.lineHeight) / 2;
        int textSpace = getContentWidth() - PADDING - SKIN_SIZE - PADDING - PADDING - SLIDER_WIDTH - PADDING;
        int textWidth = minecraft.font.width(text);
        if (textWidth > textSpace) {
            guiGraphics.textRenderer().acceptScrollingWithDefaultCenter(text, textX, textX + textSpace, textY, textY + minecraft.font.lineHeight);
        } else {
            guiGraphics.drawString(minecraft.font, text, textX, textY, 0xFFFFFFFF, false);
        }
    }

}
