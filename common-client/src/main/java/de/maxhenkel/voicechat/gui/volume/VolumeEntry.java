package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public abstract class VolumeEntry extends ListScreenEntryBase {

    protected static final int SKIN_SIZE = 24;
    protected static final int PADDING = 4;
    protected static final int SLIDER_WIDTH = 90;
    protected static final int ARROW_SIZE = 6;
    protected static final int ARROW_SPACING = 1;
    protected static final int ARROW_COLUMN_WIDTH = ARROW_SPACING + ARROW_SIZE + ARROW_SPACING;
    protected static final int BG_FILL = VoiceChatScreenBase.color(255, 74, 74, 74);
    protected static final int PLAYER_NAME_COLOR = VoiceChatScreenBase.color(255, 255, 255, 255);

    protected final Minecraft minecraft;
    protected final AdjustVolumesScreen screen;
    protected final AdjustVolumeSlider volumeSlider;
    protected final boolean indented;
    protected final boolean offset;

    public VolumeEntry(AdjustVolumesScreen screen, AdjustVolumeSlider.AdjustVolumeEntry entry, boolean indented, boolean offset) {
        this.minecraft = Minecraft.getMinecraft();
        this.screen = screen;
        this.indented = indented;
        this.offset = offset;
        this.volumeSlider = new AdjustVolumeSlider(0, 0, 0, SLIDER_WIDTH, 20, entry);
        this.children.add(volumeSlider);
    }

    @Override
    public void drawEntry(int slotIndex, int rowLeft, int y, int rowWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        super.drawEntry(slotIndex, rowLeft, y, rowWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        int indent = indented ? ARROW_COLUMN_WIDTH - PADDING : 0;
        int x = rowLeft + indent;
        int listWidth = rowWidth - indent;
        int skinX = x + (offset ? ARROW_COLUMN_WIDTH : PADDING);
        int skinY = y + (slotHeight - SKIN_SIZE) / 2;
        int textX = skinX + SKIN_SIZE + PADDING;
        int textY = y + (slotHeight - minecraft.fontRenderer.FONT_HEIGHT) / 2;

        GuiScreen.drawRect(x, y, x + listWidth, y + slotHeight, BG_FILL);

        volumeSlider.x = x + listWidth - volumeSlider.width - PADDING;
        volumeSlider.y = y + (slotHeight - volumeSlider.height) / 2;

        renderElement(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks, skinX, skinY, textX, textY);

        volumeSlider.drawButton(minecraft, mouseX, mouseY, partialTicks);
    }

    public abstract void renderElement(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks, int skinX, int skinY, int textX, int textY);

}
