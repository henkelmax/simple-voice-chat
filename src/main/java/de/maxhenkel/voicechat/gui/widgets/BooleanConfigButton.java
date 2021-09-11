package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Function;

public class BooleanConfigButton extends AbstractButton {

    private ForgeConfigSpec.BooleanValue entry;
    private Function<Boolean, Component> component;

    public BooleanConfigButton(int x, int y, int width, int height, ForgeConfigSpec.BooleanValue entry, Function<Boolean, Component> component) {
        super(x, y, width, height, TextComponent.EMPTY);
        this.entry = entry;
        this.component = component;
        updateText();
    }

    private void updateText() {
        setMessage(component.apply(entry.get()));
    }

    @Override
    public void onPress() {
        entry.set(!entry.get());
        entry.save();
        updateText();
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
