package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.Minecraft;

public abstract class DebouncedSlider extends Slider {

    private boolean dragged;
    private double lastValue;

    public DebouncedSlider(int buttonId, int x, int y, int width, int height, double value) {
        super(buttonId, x, y, width, height, value);
        lastValue = value;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean b = super.mousePressed(mc, mouseX, mouseY);
        applyDebouncedInternal();
        return b;
    }

    @Override
    public void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        super.mouseDragged(mc, mouseX, mouseY);
        dragged = true;
        if (value >= 1D || value <= 0D) {
            applyDebouncedInternal();
            dragged = false;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        super.mouseReleased(mouseX, mouseY);
        if (dragged) {
            applyDebouncedInternal();
            dragged = false;
        }
    }

    private void applyDebouncedInternal() {
        if (value == lastValue) {
            return;
        }
        lastValue = value;
        applyDebounced();
    }

    public abstract void applyDebounced();

}
