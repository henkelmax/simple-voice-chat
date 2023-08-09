package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.function.Function;

public class BooleanConfigButton extends ButtonBase {

    private ConfigEntry<Boolean> entry;
    private Function<Boolean, ITextComponent> component;

    public BooleanConfigButton(int id, int x, int y, int width, int height, ConfigEntry<Boolean> entry, Function<Boolean, ITextComponent> component) {
        super(id, x, y, width, height, new TextComponentString(""));
        this.entry = entry;
        this.component = component;
        updateText();
    }

    private void updateText() {
        displayString = component.apply(entry.get()).getUnformattedComponentText();
    }

    @Override
    public void onPress() {
        entry.set(!entry.get()).save();
        updateText();
    }

}
