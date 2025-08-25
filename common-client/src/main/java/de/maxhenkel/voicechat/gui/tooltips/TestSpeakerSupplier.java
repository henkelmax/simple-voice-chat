package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class TestSpeakerSupplier implements ImageButton.TooltipSupplier {

    public static final ITextComponent TEST_SPEAKER = new TextComponentTranslation("message.voicechat.test_speaker");

    @Override
    public void onTooltip(ImageButton button, int mouseX, int mouseY) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen == null) {
            return;
        }
        screen.drawHoveringText(TEST_SPEAKER.getUnformattedComponentText(), mouseX, mouseY);
    }
}
