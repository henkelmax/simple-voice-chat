package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class GroupChatModeTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final Component GROUP_CHAT_MODE = Component.translatable("message.voicechat.group_chat_mode_tooltip");
    public static final Component PROXIMITY_CHAT_MODE = Component.translatable("message.voicechat.proximity_chat_mode_tooltip");

    private final Screen screen;
    @Nullable
    private Boolean lastState;

    public GroupChatModeTooltipSupplier(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void updateTooltip(ImageButton button) {
        boolean groupChatOnly = VoicechatClient.CLIENT_CONFIG.groupChatOnly.get();
        if (lastState == null || lastState != groupChatOnly) {
            lastState = groupChatOnly;
            button.setTooltip(Tooltip.create(groupChatOnly ? GROUP_CHAT_MODE : PROXIMITY_CHAT_MODE));
        }
    }

}
