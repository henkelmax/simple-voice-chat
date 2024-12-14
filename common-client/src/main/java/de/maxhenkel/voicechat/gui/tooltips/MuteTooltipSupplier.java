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
            tooltip.add(new TranslatableComponent("message.voicechat.mute.disabled_ptt").getVisualOrderText());
        } else if (stateManager.isMuted()) {
            tooltip.add(new TranslatableComponent("message.voicechat.mute.enabled").getVisualOrderText());
        } else {
            tooltip.add(new TranslatableComponent("message.voicechat.mute.disabled").getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

    public static boolean canMuteMic() {
        return VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
    }

}
