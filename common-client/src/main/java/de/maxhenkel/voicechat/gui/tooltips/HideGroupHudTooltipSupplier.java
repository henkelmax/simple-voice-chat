package de.maxhenkel.voicechat.gui.tooltips;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class HideGroupHudTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final Component SHOW_GROUP_HUD_ENABLED = Component.translatable("message.voicechat.show_group_hud.enabled");
    public static final Component SHOW_GROUP_HUD_DISABLED = Component.translatable("message.voicechat.show_group_hud.disabled");


    private final Screen screen;

    public HideGroupHudTooltipSupplier(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()) {
            tooltip.add(SHOW_GROUP_HUD_ENABLED.getVisualOrderText());
        } else {
            tooltip.add(SHOW_GROUP_HUD_DISABLED.getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

}
