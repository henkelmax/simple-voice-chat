package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Function;

public class BooleanConfigButton extends AbstractButton {

    private ConfigEntry<Boolean> entry;
    private Function<Boolean, ITextComponent> component;

    public BooleanConfigButton(int x, int y, int width, int height, ConfigEntry<Boolean> entry, Function<Boolean, ITextComponent> component) {
        super(x, y, width, height, new StringTextComponent(""));
        this.entry = entry;
        this.component = component;
        updateText();
    }

    private void updateText() {
        setMessage(component.apply(entry.get()));
    }

    @Override
    public void onPress() {
        entry.set(!entry.get()).save();
        updateText();
    }

}
