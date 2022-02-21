package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public abstract class DebouncedSlider extends AbstractSlider {

    private boolean dragged;
    private double lastValue;

    public DebouncedSlider(int i, int j, int k, int l, ITextComponent component, double d) {
        super(i, j, k, l, component, d);
        lastValue = d;
    }

    @Override
    public boolean keyPressed(int keyCode, int j, int k) {
        boolean result = super.keyPressed(keyCode, j, k);
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
            applyDebouncedInternal();
        }
        return result;
    }

    @Override
    public void onClick(double d, double e) {
        super.onClick(d, e);
        applyDebouncedInternal();
    }

    @Override
    protected void onDrag(double d, double e, double f, double g) {
        super.onDrag(d, e, f, g);
        dragged = true;
        if (value >= 1D || value <= 0D) {
            applyDebouncedInternal();
            dragged = false;
        }
    }

    @Override
    public void onRelease(double d, double e) {
        super.onRelease(d, e);
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

    @Override
    protected void applyValue() {

    }
}
