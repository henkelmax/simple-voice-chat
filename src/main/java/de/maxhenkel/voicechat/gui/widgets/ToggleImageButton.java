package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class ToggleImageButton extends ImageButton {

    protected Supplier<Boolean> stateSupplier;

    public ToggleImageButton(int x, int y, Identifier texture, Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, texture, onPress, tooltipSupplier);
        this.stateSupplier = stateSupplier;
    }

    @Override
    protected void renderImage(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        mc.getTextureManager().bindTexture(texture);

        if (stateSupplier.get()) {
            drawTexture(matrices, x + 2, y + 2, 16, 0, 16, 16, 32, 32);
        } else {
            drawTexture(matrices, x + 2, y + 2, 0, 0, 16, 16, 32, 32);
        }
    }

}
