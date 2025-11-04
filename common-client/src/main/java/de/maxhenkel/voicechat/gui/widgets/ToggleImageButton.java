package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ToggleImageButton extends ImageButton {

    @Nullable
    protected Supplier<Boolean> stateSupplier;

    public ToggleImageButton(int x, int y, Identifier texture, @Nullable Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, texture, onPress, tooltipSupplier);
        this.stateSupplier = stateSupplier;
    }

    @Override
    protected void renderImage(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (stateSupplier == null) {
            return;
        }

        if (stateSupplier.get()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX() + 2, getY() + 2, 16, 0, 16, 16, 32, 32);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX() + 2, getY() + 2, 0, 0, 16, 16, 32, 32);
        }
    }

}
