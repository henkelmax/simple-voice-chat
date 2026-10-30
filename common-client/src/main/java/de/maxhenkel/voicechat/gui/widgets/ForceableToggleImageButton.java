package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public class ForceableToggleImageButton extends ToggleImageButton {

    public ForceableToggleImageButton(int id, int x, int y, ResourceLocation texture, Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(id, x, y, texture, stateSupplier, onPress, tooltipSupplier);
    }

    @Override
    protected void renderImage(int mouseX, int mouseY, float delta) {
        if (!enabled) {
            mc.getTextureManager().bindTexture(texture);
            drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0, 16, 16, 16, 32, 32);
            return;
        }
        super.renderImage(mouseX, mouseY, delta);
    }

}
