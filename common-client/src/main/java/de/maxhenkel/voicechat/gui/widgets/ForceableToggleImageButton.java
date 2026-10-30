package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ForceableToggleImageButton extends ToggleImageButton {

    public ForceableToggleImageButton(int x, int y, ResourceLocation texture, Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, texture, stateSupplier, onPress, tooltipSupplier);
    }

    @Override
    protected void renderImage(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!active) {
            RenderSystem.setShaderTexture(0, texture);
            blit(matrices, x + 2, y + 2, 0, 16, 16, 16, 32, 32);
            return;
        }
        super.renderImage(matrices, mouseX, mouseY, delta);
    }

}
