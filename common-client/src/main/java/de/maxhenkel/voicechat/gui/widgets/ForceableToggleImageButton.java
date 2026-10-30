package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ForceableToggleImageButton extends ToggleImageButton {

    public ForceableToggleImageButton(int x, int y, ResourceLocation texture, Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, texture, stateSupplier, onPress, tooltipSupplier);
    }

    @Override
    protected void renderImage(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!active) {
            guiGraphics.blitSprite(texture, 32, 32, 0, 16, getX() + 2, getY() + 2, 16, 16);
            return;
        }
        super.renderImage(guiGraphics, mouseX, mouseY);
    }

}
