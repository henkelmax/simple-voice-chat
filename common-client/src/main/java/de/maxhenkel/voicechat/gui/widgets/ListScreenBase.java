package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public abstract class ListScreenBase extends VoiceChatScreenBase {

    private Runnable postRender;

    public ListScreenBase(Component title, int xSize, int ySize) {
        super(title, xSize, ySize);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        if (postRender != null) {
            postRender.run();
            postRender = null;
        }
    }

    //TODO Remove
    public void postRender(Runnable postRender) {
        this.postRender = postRender;
    }

}
