package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

import java.util.List;

public class ExpandableCategoryVolumeEntry extends CategoryVolumeEntry {

    protected final boolean expanded;
    protected final Runnable onToggle;

    public ExpandableCategoryVolumeEntry(VolumeCategoryImpl category, List<VolumeCategoryImpl> children, AdjustVolumesScreen screen, boolean expanded, Runnable onToggle) {
        super(category, children, screen, false, true);
        this.expanded = expanded;
        this.onToggle = onToggle;
    }

    @Override
    public void renderElement(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        super.renderElement(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovered, delta, skinX, skinY, textX, textY);
        boolean highlighted = hovered && !volumeSlider.isMouseOver(mouseX, mouseY);
        int u = 236 + (highlighted ? ARROW_SIZE : 0);
        int v = 12 + (expanded ? ARROW_SIZE : 0);
        guiGraphics.blit(RenderType::guiTextured, AdjustVolumesScreen.TEXTURE, left + ARROW_SPACING, top + (height - ARROW_SIZE) / 2, u, v, ARROW_SIZE, ARROW_SIZE, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        onToggle.run();
        return true;
    }

}
