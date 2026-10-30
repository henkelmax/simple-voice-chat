package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public class ForceableToggleImageButton extends ToggleImageButton {

    public ForceableToggleImageButton(int x, int y, ResourceLocation texture, Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, texture, stateSupplier, onPress, tooltipSupplier);
    }

    @Override
    protected void renderImage(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!active) {
            mc.getTextureManager().bind(texture);
            blit(matrices, x + 2, y + 2, 0, 16, 16, 16, 32, 32);
            return;
        }
        super.renderImage(matrices, mouseX, mouseY, delta);
    }

}
