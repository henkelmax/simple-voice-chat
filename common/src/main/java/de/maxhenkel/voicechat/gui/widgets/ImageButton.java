package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

public class ImageButton extends ButtonBase {

    protected Minecraft mc;
    protected ResourceLocation texture;
    protected PressAction onPress;
    protected TooltipSupplier tooltipSupplier;

    public ImageButton(int id, int x, int y, ResourceLocation texture, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(id, x, y, 20, 20, new TextComponentString(""));
        mc = Minecraft.getMinecraft();
        this.texture = texture;
        this.onPress = onPress;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    protected void renderImage(int mouseX, int mouseY, float delta) {
        mc.getTextureManager().bindTexture(texture);
        drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float delta) {
        super.drawButton(minecraft, mouseX, mouseY, delta);
        renderImage(mouseX, mouseY, delta);
    }

    @Override
    public void renderTooltips(int mouseX, int mouseY, float delta) {
        if (hovered) {
            tooltipSupplier.onTooltip(this, mouseX, mouseY);
        }
    }

    public interface TooltipSupplier {
        void onTooltip(ImageButton button, int mouseX, int mouseY);
    }

    public interface PressAction {
        void onPress(ImageButton button);
    }

}
