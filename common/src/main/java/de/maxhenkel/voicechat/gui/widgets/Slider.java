package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public abstract class Slider extends GuiButton {

    protected double value;
    private boolean dragging;

    public Slider(int buttonId, int x, int y, int width, int height, double value) {
        super(buttonId, x, y, width, height, "");
        this.value = value;
    }

    @Override
    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            if (dragging) {
                updateSliderValue(mouseX, mouseY);
            }
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
        if (!visible) {
            return;
        }
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawTexturedModalRect(x + (int) (value * (float) (width - 8)), y, 0, 66, 4, 20);
        drawTexturedModalRect(x + (int) (value * (float) (width - 8)) + 4, y, 196, 66, 4, 20);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            updateSliderValue(mouseX, mouseY);
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    private void updateSliderValue(int mouseX, int mouseY) {
        value = (double) (mouseX - (x + 4)) / (double) (width - 8);
        value = MathHelper.clamp(value, 0D, 1D);
        updateMessage();
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    protected abstract void updateMessage();

}
