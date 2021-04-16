package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class ImageButton extends AbstractPressableButtonWidget {

    protected MinecraftClient mc;
    protected Identifier texture;
    protected PressAction onPress;
    protected TooltipSupplier tooltipSupplier;

    public ImageButton(int x, int y, Identifier texture, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, 20, 20, LiteralText.EMPTY);
        mc = MinecraftClient.getInstance();
        this.texture = texture;
        this.onPress = onPress;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    protected void renderImage(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        mc.getTextureManager().bindTexture(texture);
        drawTexture(matrices, x + 2, y + 2, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        renderImage(matrices, mouseX, mouseY, delta);

        if (isHovered()) {
            renderToolTip(matrices, mouseX, mouseY);
        }
    }

    public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
        this.tooltipSupplier.onTooltip(this, matrices, mouseX, mouseY);
    }

    public interface TooltipSupplier {
        void onTooltip(ImageButton button, MatrixStack matrices, int mouseX, int mouseY);
    }

    public interface PressAction {
        void onPress(ImageButton button);
    }

}
