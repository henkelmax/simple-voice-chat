package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class ForceableToggleImageButton extends ToggleImageButton {

    public ForceableToggleImageButton(int x, int y, Identifier texture, Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, texture, stateSupplier, onPress, tooltipSupplier);
    }

    @Override
    protected void extractImage(GuiGraphicsExtractor guiGraphics) {
        if (!active) {
            guiGraphics.blitSprite(GuiPipeline.TEXTURED, texture, 32, 32, 0, 16, getX() + 2, getY() + 2, 16, 16);
            return;
        }
        super.extractImage(guiGraphics);
    }

}
