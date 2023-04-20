package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class RecordingTooltipSupplier implements ImageButton.TooltipSupplier {

    private final Screen screen;

    public RecordingTooltipSupplier(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return;
        }

        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (client.getRecorder() == null) {
            tooltip.add(Component.translatable("message.voicechat.recording.disabled").getVisualOrderText());
        } else {
            tooltip.add(Component.translatable("message.voicechat.recording.enabled").getVisualOrderText());
        }

        guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
    }

}
