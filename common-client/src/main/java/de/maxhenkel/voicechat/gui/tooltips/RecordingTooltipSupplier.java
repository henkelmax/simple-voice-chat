package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class RecordingTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final Component RECORDING_ENABLED = Component.translatable("message.voicechat.recording.enabled");
    public static final Component RECORDING_DISABLED = Component.translatable("message.voicechat.recording.disabled");

    private final Screen screen;
    @Nullable
    private Boolean lastState;

    public RecordingTooltipSupplier(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void updateTooltip(ImageButton button) {
        ClientVoicechat client = ClientManager.getClient();
        boolean recording = client != null && client.getRecorder() != null;
        if (lastState == null || lastState != recording) {
            lastState = recording;
            button.setTooltip(Tooltip.create(recording ? RECORDING_ENABLED : RECORDING_DISABLED));
        }
    }

}
