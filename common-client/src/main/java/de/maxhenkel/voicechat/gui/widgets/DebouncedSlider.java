package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class DebouncedSlider extends AbstractSliderButton {

    private boolean dragged;
    private double lastValue;

    public DebouncedSlider(int i, int j, int k, int l, Component component, double d) {
        super(i, j, k, l, component, d);
        lastValue = d;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        boolean result = super.keyPressed(keyEvent);
        if (keyEvent.isLeft() || keyEvent.isRight()) {
            applyDebouncedInternal();
        }
        return result;
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
        super.onClick(mouseButtonEvent, bl);
        applyDebouncedInternal();
    }

    @Override
    protected void onDrag(MouseButtonEvent mouseButtonEvent, double d, double e) {
        super.onDrag(mouseButtonEvent, d, e);
        dragged = true;
        if (value >= 1D || value <= 0D) {
            applyDebouncedInternal();
            dragged = false;
        }
    }

    @Override
    public void onRelease(MouseButtonEvent mouseButtonEvent) {
        super.onRelease(mouseButtonEvent);
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
