package de.maxhenkel.voicechat.gui.tooltips;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TestSpeakerSupplier implements ImageButton.TooltipSupplier {

    public static final ITextComponent TEST_SPEAKER = new TranslationTextComponent("message.voicechat.test_speaker");

    @Override
    public void onTooltip(ImageButton button, MatrixStack matrices, int mouseX, int mouseY) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) {
            return;
        }
        screen.renderTooltip(matrices, TEST_SPEAKER, mouseX, mouseY);
    }
}
