package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class DisableTooltipSupplier implements ImageButton.TooltipSupplier {

    private final GuiScreen screen;
    private final ClientPlayerStateManager stateManager;

    public DisableTooltipSupplier(GuiScreen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void onTooltip(ImageButton button, int mouseX, int mouseY) {
        List<String> tooltip = new ArrayList<>();

        if (!stateManager.canEnable()) {
            tooltip.add(new TextComponentTranslation("message.voicechat.disable.no_speaker").getUnformattedComponentText());
        } else if (stateManager.isDisabled()) {
            tooltip.add(new TextComponentTranslation("message.voicechat.disable.enabled").getUnformattedComponentText());
        } else {
            tooltip.add(new TextComponentTranslation("message.voicechat.disable.disabled").getUnformattedComponentText());
        }

        screen.drawHoveringText(tooltip, mouseX, mouseY);
    }

}
