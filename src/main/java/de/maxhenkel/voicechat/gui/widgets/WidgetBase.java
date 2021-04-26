package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;

public abstract class WidgetBase {

    protected VoiceChatScreenBase screen;
    protected Minecraft mc;
    protected int posX, posY, width, height, guiLeft, guiTop, xSize, ySize;

    public WidgetBase(VoiceChatScreenBase screen, int posX, int posY, int xSize, int ySize) {
        this.screen = screen;
        mc = Minecraft.getInstance();
        this.posX = posX;
        this.posY = posY;
        this.xSize = xSize;
        this.ySize = ySize;

        width = screen.width;
        height = screen.height;
        guiLeft = screen.getGuiLeft() + posX;
        guiTop = screen.getGuiTop() + posY;
    }

    public void tick() {

    }

    protected void drawGuiContainerForegroundLayer(PoseStack matrixStack, int mouseX, int mouseY) {

    }

    protected void drawGuiContainerBackgroundLayer(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {

    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }

    protected void addWidget(AbstractWidget widget) {
        screen.addButton(widget);
    }

}
