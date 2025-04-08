package de.maxhenkel.voicechat.gui.tooltips;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class HideTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final Component HIDE_ICONS_ENABLED = new TranslatableComponent("message.voicechat.hide_icons.enabled");
    public static final Component HIDE_ICONS_DISABLED = new TranslatableComponent("message.voicechat.hide_icons.disabled");

    private final Screen screen;

    public HideTooltipSupplier(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            tooltip.add(HIDE_ICONS_ENABLED.getVisualOrderText());
        } else {
            tooltip.add(HIDE_ICONS_DISABLED.getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

}
