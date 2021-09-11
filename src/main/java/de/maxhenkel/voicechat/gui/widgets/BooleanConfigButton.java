package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Function;

public class BooleanConfigButton extends AbstractButton {

    private ForgeConfigSpec.BooleanValue entry;
    private Function<Boolean, ITextComponent> component;

    public BooleanConfigButton(int x, int y, int width, int height, ForgeConfigSpec.BooleanValue entry, Function<Boolean, ITextComponent> component) {
        super(x, y, width, height, StringTextComponent.EMPTY);
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
}
