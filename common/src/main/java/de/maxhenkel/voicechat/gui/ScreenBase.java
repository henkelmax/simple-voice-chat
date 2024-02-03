package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.gui.widgets.ButtonBase;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;

import java.io.IOException;

public abstract class ScreenBase extends GuiScreen {

    protected ITextComponent title;

    protected ScreenBase(ITextComponent title) {
        this.title = title;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        labelList.clear();
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float delta) {
        drawDefaultBackground();
        renderBackground(mouseX, mouseY, delta);
        super.drawScreen(mouseX, mouseY, delta);
        renderForeground(mouseX, mouseY, delta);
        for (GuiButton button : buttonList) {
            if (button instanceof ButtonBase) {
                ((ButtonBase) button).renderTooltips(mouseX, mouseY, delta);
            }
        }
    }

    public void renderBackground(int mouseX, int mouseY, float delta) {

    }

    public void renderForeground(int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (!(button instanceof ButtonBase)) {
            return;
        }

        ButtonBase b = (ButtonBase) button;
        b.onPress();
    }

    protected boolean isIngame() {
        return mc.world != null;
    }

    public static int color(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

}
