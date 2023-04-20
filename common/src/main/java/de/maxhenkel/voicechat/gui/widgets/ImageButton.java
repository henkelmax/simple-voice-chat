package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class ImageButton extends AbstractButton {

    protected Minecraft mc;
    protected ResourceLocation texture;
    protected PressAction onPress;
    @Nullable
    protected TooltipSupplier tooltipSupplier;

    public ImageButton(int x, int y, ResourceLocation texture, PressAction onPress, @Nullable TooltipSupplier tooltipSupplier) {
        super(x, y, 20, 20, Component.empty());
        mc = Minecraft.getInstance();
        this.texture = texture;
        this.onPress = onPress;
        this.tooltipSupplier = tooltipSupplier;
    }

    public ImageButton(int x, int y, ResourceLocation texture, PressAction onPress) {
        this(x, y, texture, onPress, null);
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    protected void renderImage(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        guiGraphics.blit(texture, getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        super.renderWidget(guiGraphics, mouseX, mouseY, f);
        renderImage(guiGraphics, mouseX, mouseY);

        if (isHovered) {
            renderToolTip(guiGraphics, mc.font, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
        if (tooltipSupplier == null) {
            return;
        }
        tooltipSupplier.onTooltip(this, guiGraphics, font, mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    public interface TooltipSupplier {
        void onTooltip(ImageButton button, GuiGraphics guiGraphics, Font font, int mouseX, int mouseY);
    }

    public interface PressAction {
        void onPress(ImageButton button);
    }

}
