package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.ITextComponent;

public abstract class ButtonBase extends GuiButton {

    public ButtonBase(int id, int x, int y, int width, int height, ITextComponent text) {
        super(id, x, y, width, height, text.getUnformattedComponentText());
    }

    public abstract void onPress();

    public void renderTooltips(int mouseX, int mouseY, float delta) {

    }

}
