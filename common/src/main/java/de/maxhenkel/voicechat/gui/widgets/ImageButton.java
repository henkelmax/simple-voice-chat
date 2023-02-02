package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ImageButton extends AbstractButton {

    protected Minecraft mc;
    protected ResourceLocation texture;
    protected PressAction onPress;
    // TODO replace tooltip supplier with builtin button tooltip
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

    protected void renderImage(PoseStack matrices, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, texture);
        blit(matrices, getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY) {
        super.renderButton(matrices, mouseX, mouseY);
        renderImage(matrices, mouseX, mouseY);

        if (isHovered) {
            renderToolTip(matrices, mouseX, mouseY);
        }
    }

    public void renderToolTip(PoseStack matrices, int mouseX, int mouseY) {
        if (tooltipSupplier == null) {
            return;
        }
        tooltipSupplier.onTooltip(this, matrices, mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    public interface TooltipSupplier {
        void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY);
    }

    public interface PressAction {
        void onPress(ImageButton button);
    }

}
