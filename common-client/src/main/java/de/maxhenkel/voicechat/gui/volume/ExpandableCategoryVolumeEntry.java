package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import net.minecraft.client.gui.AbstractGui;
import org.lwjgl.glfw.GLFW;

public class ExpandableCategoryVolumeEntry extends CategoryVolumeEntry {

    protected final boolean expanded;
    protected final Runnable onToggle;

    public ExpandableCategoryVolumeEntry(VolumeCategoryImpl category, AdjustVolumesScreen screen, boolean expanded, Runnable onToggle) {
        super(category, screen, false, true);
        this.expanded = expanded;
        this.onToggle = onToggle;
    }

    @Override
    public void renderElement(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        super.renderElement(poseStack, index, top, left, width, height, mouseX, mouseY, hovered, delta, skinX, skinY, textX, textY);
        boolean highlighted = hovered && !volumeSlider.isMouseOver(mouseX, mouseY);
        int u = 236 + (highlighted ? ARROW_SIZE : 0);
        int v = 12 + (expanded ? ARROW_SIZE : 0);
        minecraft.getTextureManager().bind(AdjustVolumesScreen.TEXTURE);
        AbstractGui.blit(poseStack, left + ARROW_SPACING, top + (height - ARROW_SIZE) / 2, u, v, ARROW_SIZE, ARROW_SIZE, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        onToggle.run();
        return true;
    }

}
