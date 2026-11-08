package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.regex.Pattern;

public class GroupEditBox extends EditBox {

    private final Pattern pattern;

    public GroupEditBox(Font font, int x, int y, int width, int height, Pattern pattern) {
        super(font, x, y, width, height, Component.empty());
        this.pattern = pattern;
    }

    @Override
    public void insertText(String value) {
        String newText = getValue() + value;
        if (newText.isEmpty() || pattern.matcher(newText).matches()) {
            super.insertText(value);
        }
    }

}
