package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class HideGroupHudTooltipSupplier implements ImageButton.TooltipSupplier {

    private final GuiScreen screen;

    public HideGroupHudTooltipSupplier(GuiScreen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, int mouseX, int mouseY) {
        List<String> tooltip = new ArrayList<>();

        if (VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()) {
            tooltip.add(new TextComponentTranslation("message.voicechat.show_group_hud.enabled").getUnformattedComponentText());
        } else {
            tooltip.add(new TextComponentTranslation("message.voicechat.show_group_hud.disabled").getUnformattedComponentText());
        }

        screen.drawHoveringText(tooltip, mouseX, mouseY);
    }

}
