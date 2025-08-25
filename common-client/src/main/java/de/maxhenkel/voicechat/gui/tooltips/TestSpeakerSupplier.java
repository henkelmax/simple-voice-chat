package de.maxhenkel.voicechat.gui.tooltips;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TestSpeakerSupplier implements ImageButton.TooltipSupplier {

    public static final Component TEST_SPEAKER = Component.translatable("message.voicechat.test_speaker");

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) {
            return;
        }
        screen.renderTooltip(matrices, TEST_SPEAKER, mouseX, mouseY);
    }
}
