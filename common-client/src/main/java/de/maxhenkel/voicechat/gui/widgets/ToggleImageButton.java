package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ToggleImageButton extends ImageButton {

    @Nullable
    protected Supplier<Boolean> stateSupplier;

    public ToggleImageButton(int x, int y, ResourceLocation texture, @Nullable Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, texture, onPress, tooltipSupplier);
        this.stateSupplier = stateSupplier;
    }

    @Override
    protected void renderImage(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (stateSupplier == null) {
            return;
        }
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        if (stateSupplier.get()) {
            guiGraphics.blitSprite(RenderType::guiTextured, texture, 32, 32, 16, 0, getX() + 2, getY() + 2, 16, 16);
        } else {
            guiGraphics.blitSprite(RenderType::guiTextured, texture, 32, 32, 0, 0, getX() + 2, getY() + 2, 16, 16);
        }
    }

}
