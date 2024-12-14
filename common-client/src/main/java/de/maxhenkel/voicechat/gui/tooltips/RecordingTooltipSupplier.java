package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class RecordingTooltipSupplier implements ImageButton.TooltipSupplier {

    private final GuiScreen screen;

    public RecordingTooltipSupplier(GuiScreen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, int mouseX, int mouseY) {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return;
        }

        List<String> tooltip = new ArrayList<>();

        if (client.getRecorder() == null) {
            tooltip.add(new TextComponentTranslation("message.voicechat.recording.disabled").getUnformattedComponentText());
        } else {
            tooltip.add(new TextComponentTranslation("message.voicechat.recording.enabled").getUnformattedComponentText());
        }

        screen.drawHoveringText(tooltip, mouseX, mouseY);
    }

}
