package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class ImageButton extends AbstractButton {

    protected Minecraft mc;
    protected ResourceLocation texture;
    @Nullable
    protected PressAction onPress;
    protected TooltipSupplier tooltipSupplier;

    public ImageButton(int x, int y, ResourceLocation texture, @Nullable PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, 20, 20, Component.empty());
        mc = Minecraft.getInstance();
        this.texture = texture;
        this.onPress = onPress;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void onPress() {
        if (onPress != null) {
            onPress.onPress(this);
        }
    }

    protected void renderImage(PoseStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, texture);
        blit(matrices, x + 2, y + 2, 0, 0, 16, 16, 16, 16);
    }

    protected boolean shouldRenderTooltip() {
        return isHovered;
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        renderImage(matrices, mouseX, mouseY, delta);

        if (shouldRenderTooltip()) {
            renderToolTip(matrices, mouseX, mouseY);
        }
    }

    public void renderToolTip(PoseStack matrices, int mouseX, int mouseY) {
        this.tooltipSupplier.onTooltip(this, matrices, mouseX, mouseY);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public interface TooltipSupplier {
        void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY);
    }

    public interface PressAction {
        void onPress(ImageButton button);
    }

}
