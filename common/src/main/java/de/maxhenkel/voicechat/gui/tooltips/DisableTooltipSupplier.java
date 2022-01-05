package de.maxhenkel.voicechat.gui.tooltips;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
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
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (stateManager.isDisabled()) {
            tooltip.add(new TranslatableComponent("message.voicechat.disable.enabled").getVisualOrderText());
        } else {
            tooltip.add(new TranslatableComponent("message.voicechat.disable.disabled").getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

}
