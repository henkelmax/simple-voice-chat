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

    private final Screen screen;

    public HideGroupHudTooltipSupplier(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()) {
            tooltip.add(Component.translatable("message.voicechat.show_group_hud.enabled").getVisualOrderText());
        } else {
            tooltip.add(Component.translatable("message.voicechat.show_group_hud.disabled").getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

}
