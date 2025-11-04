package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class ImageButton extends AbstractButton {

    protected Minecraft mc;
    protected Identifier texture;
    @Nullable
    protected PressAction onPress;
    @Nullable
    protected TooltipSupplier tooltipSupplier;

    public ImageButton(int x, int y, Identifier texture, @Nullable PressAction onPress, @Nullable TooltipSupplier tooltipSupplier) {
        super(x, y, 20, 20, Component.empty());
        mc = Minecraft.getInstance();
        this.texture = texture;
        this.onPress = onPress;
        this.tooltipSupplier = tooltipSupplier;
    }

    public ImageButton(int x, int y, Identifier texture, PressAction onPress) {
        this(x, y, texture, onPress, null);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (onPress != null) {
            onPress.onPress(this);
        }
    }

    protected void renderImage(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        renderDefaultSprite(guiGraphics);
        renderImage(guiGraphics, mouseX, mouseY);

        if (tooltipSupplier != null) {
            tooltipSupplier.updateTooltip(this);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    public interface TooltipSupplier {
        void updateTooltip(ImageButton button);
    }

    public interface PressAction {
        void onPress(ImageButton button);
    }

}
