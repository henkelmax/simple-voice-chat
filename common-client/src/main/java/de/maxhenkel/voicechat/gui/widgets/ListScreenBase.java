package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.network.chat.Component;

public abstract class ListScreenBase extends VoiceChatScreenBase {

    private Runnable postRender;

    public ListScreenBase(Component title, int xSize, int ySize) {
        super(title, xSize, ySize);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        if (postRender != null) {
            postRender.run();
            postRender = null;
        }
    }

    public void postRender(Runnable postRender) {
        this.postRender = postRender;
    }

}
