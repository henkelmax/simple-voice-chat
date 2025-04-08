package de.maxhenkel.voicechat.gui.tooltips;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class RecordingTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final Component RECORDING_ENABLED = new TranslatableComponent("message.voicechat.recording.enabled");
    public static final Component RECORDING_DISABLED = new TranslatableComponent("message.voicechat.recording.disabled");

    private final Screen screen;

    public RecordingTooltipSupplier(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return;
        }

        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (client.getRecorder() == null) {
            tooltip.add(RECORDING_DISABLED.getVisualOrderText());
        } else {
            tooltip.add(RECORDING_ENABLED.getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

}
