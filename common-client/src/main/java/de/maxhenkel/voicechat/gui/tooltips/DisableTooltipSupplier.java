package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class DisableTooltipSupplier implements ImageButton.TooltipSupplier {

    private final Screen screen;
    private final ClientPlayerStateManager stateManager;

    public DisableTooltipSupplier(Screen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void onTooltip(ImageButton button, GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (!stateManager.canEnable()) {
            tooltip.add(Component.translatable("message.voicechat.disable.no_speaker").getVisualOrderText());
        } else if (stateManager.isDisabled()) {
            tooltip.add(Component.translatable("message.voicechat.disable.enabled").getVisualOrderText());
        } else {
            tooltip.add(Component.translatable("message.voicechat.disable.disabled").getVisualOrderText());
        }

        guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
    }

}
