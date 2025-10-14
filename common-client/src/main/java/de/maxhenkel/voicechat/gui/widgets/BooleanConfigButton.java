package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class BooleanConfigButton extends AbstractButton {

    protected ConfigEntry<Boolean> entry;
    protected Function<Boolean, Component> component;

    public BooleanConfigButton(int x, int y, int width, int height, ConfigEntry<Boolean> entry, Function<Boolean, Component> component) {
        super(x, y, width, height, Component.empty());
        this.entry = entry;
        this.component = component;
        updateText();
    }

    private void updateText() {
        setMessage(component.apply(entry.get()));
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        entry.set(!entry.get()).save();
        updateText();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        renderDefaultSprite(guiGraphics);
        renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
