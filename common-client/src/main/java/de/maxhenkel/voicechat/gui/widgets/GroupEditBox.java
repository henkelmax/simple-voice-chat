package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class GroupEditBox extends EditBox {

    public GroupEditBox(Font font, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.empty());
    }

    @Override
    public void insertText(String value) {
        if (value.isEmpty() || Voicechat.GROUP_REGEX.matcher(value).matches()) {
            super.insertText(value);
        }
    }

}
