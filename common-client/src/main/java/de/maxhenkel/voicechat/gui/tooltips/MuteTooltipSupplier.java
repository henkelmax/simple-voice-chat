package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class MuteTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final TranslationTextComponent MUTE_UNMUTED = new TranslationTextComponent("message.voicechat.mute.disabled");
    public static final TranslationTextComponent MUTE_MUTED = new TranslationTextComponent("message.voicechat.mute.enabled");
    public static final TranslationTextComponent MUTE_DISABLED_PTT = new TranslationTextComponent("message.voicechat.mute.disabled_ptt");

    private GuiScreen screen;
    private ClientPlayerStateManager stateManager;

    public MuteTooltipSupplier(GuiScreen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void onTooltip(ImageButton button, int mouseX, int mouseY) {
        List<String> tooltip = new ArrayList<>();

        if (!canMuteMic()) {
            tooltip.add(MUTE_DISABLED_PTT.getUnformattedComponentText());
        } else if (stateManager.isMuted()) {
            tooltip.add(MUTE_MUTED.getUnformattedComponentText());
        } else {
            tooltip.add(MUTE_UNMUTED.getUnformattedComponentText());
        }

        screen.drawHoveringText(tooltip, mouseX, mouseY);
    }

    public static boolean canMuteMic() {
        return VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
    }

}
