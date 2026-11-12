package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class ExpandableCategoryVolumeEntry extends CategoryVolumeEntry {

    protected final boolean expanded;
    protected final Runnable onToggle;

    public ExpandableCategoryVolumeEntry(VolumeCategoryImpl category, AdjustVolumesScreen screen, boolean expanded, Runnable onToggle) {
        super(category, screen, false, true);
        this.expanded = expanded;
        this.onToggle = onToggle;
    }

    @Override
    public void renderElement(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks, int skinX, int skinY, int textX, int textY) {
        super.renderElement(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks, skinX, skinY, textX, textY);
        boolean highlighted = isSelected && !isMouseOverSlider(mouseX, mouseY);
        int u = 236 + (highlighted ? ARROW_SIZE : 0);
        int v = 12 + (expanded ? ARROW_SIZE : 0);
        GlStateManager.color(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bindTexture(AdjustVolumesScreen.TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(x + ARROW_SPACING, y + (slotHeight - ARROW_SIZE) / 2, u, v, ARROW_SIZE, ARROW_SIZE, 256, 256);
    }

    private boolean isMouseOverSlider(int mouseX, int mouseY) {
        return mouseX >= volumeSlider.x && mouseY >= volumeSlider.y && mouseX < volumeSlider.x + volumeSlider.width && mouseY < volumeSlider.y + volumeSlider.height;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        if (super.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY)) {
            return true;
        }
        if (mouseEvent != 0) {
            return false;
        }
        onToggle.run();
        return true;
    }

}
