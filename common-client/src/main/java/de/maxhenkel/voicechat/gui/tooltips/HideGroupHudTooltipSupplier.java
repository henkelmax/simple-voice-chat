package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class HideGroupHudTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final Component SHOW_GROUP_HUD_ENABLED = Component.translatable("message.voicechat.show_group_hud.enabled");
    public static final Component SHOW_GROUP_HUD_DISABLED = Component.translatable("message.voicechat.show_group_hud.disabled");

    private final Screen screen;
    @Nullable
    private Boolean lastState;

    public HideGroupHudTooltipSupplier(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void updateTooltip(ImageButton button) {
        boolean show = VoicechatClient.CLIENT_CONFIG.showGroupHud.get();
        if (lastState == null || lastState != show) {
            lastState = show;
            button.setTooltip(Tooltip.create(show ? SHOW_GROUP_HUD_ENABLED : SHOW_GROUP_HUD_DISABLED));
        }
    }

}
