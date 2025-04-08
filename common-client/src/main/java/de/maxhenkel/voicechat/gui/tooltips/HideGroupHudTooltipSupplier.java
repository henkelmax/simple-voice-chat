package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class HideGroupHudTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final TextComponentTranslation SHOW_GROUP_HUD_ENABLED = new TextComponentTranslation("message.voicechat.show_group_hud.enabled");
    public static final TextComponentTranslation SHOW_GROUP_HUD_DISABLED = new TextComponentTranslation("message.voicechat.show_group_hud.disabled");


    private final GuiScreen screen;

    public HideGroupHudTooltipSupplier(GuiScreen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, int mouseX, int mouseY) {
        List<String> tooltip = new ArrayList<>();

        if (VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()) {
            tooltip.add(SHOW_GROUP_HUD_ENABLED.getUnformattedComponentText());
        } else {
            tooltip.add(SHOW_GROUP_HUD_DISABLED.getUnformattedComponentText());
        }

        screen.drawHoveringText(tooltip, mouseX, mouseY);
    }

}
