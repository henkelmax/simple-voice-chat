package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class EnumButton<T extends Enum<T>> extends AbstractButton {

    protected ConfigEntry<T> entry;

    public EnumButton(int xIn, int yIn, int widthIn, int heightIn, ConfigEntry<T> entry) {
        super(xIn, yIn, widthIn, heightIn, Component.empty());
        this.entry = entry;
        updateText();
    }

    protected void updateText() {
        setMessage(getText(entry.get()));
    }

    protected abstract Component getText(T type);

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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
