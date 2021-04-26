package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ToggleImageButton extends ImageButton {

    protected Supplier<Boolean> stateSupplier;

    public ToggleImageButton(int x, int y, ResourceLocation texture, Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, texture, onPress, tooltipSupplier);
        this.stateSupplier = stateSupplier;
    }

    @Override
    protected void renderImage(PoseStack matrices, int mouseX, int mouseY, float delta) {
        mc.getTextureManager().bind(texture);

        if (stateSupplier.get()) {
            blit(matrices, x + 2, y + 2, 16, 0, 16, 16, 32, 32);
        } else {
            blit(matrices, x + 2, y + 2, 0, 0, 16, 16, 32, 32);
        }
    }

}
