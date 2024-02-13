package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ToggleImageButton extends ImageButton {

    @Nullable
    protected Supplier<Boolean> stateSupplier;

    public ToggleImageButton(int id, int x, int y, ResourceLocation texture, @Nullable Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(id, x, y, texture, onPress, tooltipSupplier);
        this.stateSupplier = stateSupplier;
    }

    @Override
    protected void renderImage(int mouseX, int mouseY, float delta) {
        if (stateSupplier == null) {
            return;
        }
        mc.getTextureManager().bindTexture(texture);

        if (stateSupplier.get()) {
            drawModalRectWithCustomSizedTexture(x + 2, y + 2, 16, 0, 16, 16, 32, 32);
        } else {
            drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0, 0, 16, 16, 32, 32);
        }
    }

}
