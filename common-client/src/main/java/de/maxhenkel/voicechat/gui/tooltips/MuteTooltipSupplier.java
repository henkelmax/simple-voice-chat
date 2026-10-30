package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class MuteTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final TextComponentTranslation MUTE_UNMUTED = new TextComponentTranslation("message.voicechat.mute.disabled");
    public static final TextComponentTranslation MUTE_MUTED = new TextComponentTranslation("message.voicechat.mute.enabled");
    public static final TextComponentTranslation MUTE_FORCED = new TextComponentTranslation("message.voicechat.mute.forced");

    private GuiScreen screen;
    private ClientPlayerStateManager stateManager;

    public MuteTooltipSupplier(GuiScreen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void onTooltip(ImageButton button, int mouseX, int mouseY) {
        List<String> tooltip = new ArrayList<>();

        if (stateManager.isForceMuted()) {
            tooltip.add(MUTE_FORCED.getUnformattedComponentText());
        } else if (stateManager.isMuted()) {
            tooltip.add(MUTE_MUTED.getUnformattedComponentText());
        } else {
            tooltip.add(MUTE_UNMUTED.getUnformattedComponentText());
        }

        screen.drawHoveringText(tooltip, mouseX, mouseY);
    }

}
