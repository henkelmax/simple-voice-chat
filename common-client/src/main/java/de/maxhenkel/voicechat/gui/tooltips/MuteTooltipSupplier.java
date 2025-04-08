package de.maxhenkel.voicechat.gui.tooltips;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class MuteTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final Component MUTE_UNMUTED = new TranslatableComponent("message.voicechat.mute.disabled");
    public static final Component MUTE_MUTED = new TranslatableComponent("message.voicechat.mute.enabled");
    public static final Component MUTE_DISABLED_PTT = new TranslatableComponent("message.voicechat.mute.disabled_ptt");

    private Screen screen;
    private ClientPlayerStateManager stateManager;

    public MuteTooltipSupplier(Screen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (!canMuteMic()) {
            tooltip.add(MUTE_DISABLED_PTT.getVisualOrderText());
        } else if (stateManager.isMuted()) {
            tooltip.add(MUTE_MUTED.getVisualOrderText());
        } else {
            tooltip.add(MUTE_UNMUTED.getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

    public static boolean canMuteMic() {
        return VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
    }

}
