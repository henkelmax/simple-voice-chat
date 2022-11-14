package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.ITextComponent;

import java.io.IOException;

public abstract class ListScreenBase extends VoiceChatScreenBase {

    private Runnable postRender;
    private ListScreenListBase<?> list;

    public ListScreenBase(ITextComponent title, int xSize, int ySize) {
        super(title, xSize, ySize);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (list != null) {
            list.handleMouseInput();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (list != null) {
            list.actionPerformed(button);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float delta) {
        super.drawScreen(mouseX, mouseY, delta);
        if (list != null) {
            list.drawScreen(mouseX, mouseY, delta);
        }
        if (postRender != null) {
            postRender.run();
            postRender = null;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (list != null) {
            list.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (list != null) {
            list.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        super.onResize(mcIn, w, h);
    }

    public void setList(ListScreenListBase<?> list) {
        this.list = list;
    }

    public void removeList() {
        this.list = null;
    }

    public void postRender(Runnable postRender) {
        this.postRender = postRender;
    }

}
