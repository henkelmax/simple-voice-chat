package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.configbuilder.ConfigEntry;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public abstract class EnumButton<T extends Enum<T>> extends AbstractButton {

    private ConfigEntry<T> entry;

    public EnumButton(int xIn, int yIn, int widthIn, int heightIn, ConfigEntry<T> entry) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""));
        this.entry = entry;
        updateText();
    }

    protected void updateText() {
        setMessage(getText(entry.get()));
    }

    protected abstract ITextComponent getText(T type);

    protected void onUpdate(T type) {

    }

    @Override
    public void onPress() {
        T e = entry.get();
        Enum<T>[] values = e.getClass().getEnumConstants();
        T type = (T) values[(e.ordinal() + 1) % values.length];
        entry.set(type).save();
        updateText();
        onUpdate(type);
    }

}
