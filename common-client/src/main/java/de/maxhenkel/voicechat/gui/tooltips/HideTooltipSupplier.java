package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class HideTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final TextComponentTranslation HIDE_ICONS_ENABLED = new TextComponentTranslation("message.voicechat.hide_icons.enabled");
    public static final TextComponentTranslation HIDE_ICONS_DISABLED = new TextComponentTranslation("message.voicechat.hide_icons.disabled");

    private final GuiScreen screen;

    public HideTooltipSupplier(GuiScreen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, int mouseX, int mouseY) {
        List<String> tooltip = new ArrayList<>();

        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            tooltip.add(HIDE_ICONS_ENABLED.getUnformattedComponentText());
        } else {
            tooltip.add(HIDE_ICONS_DISABLED.getUnformattedComponentText());
        }

        screen.drawHoveringText(tooltip, mouseX, mouseY);
    }

}
