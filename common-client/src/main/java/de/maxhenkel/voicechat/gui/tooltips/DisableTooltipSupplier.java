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

    public static final TranslatableComponent DISABLE_ENABLED = new TranslatableComponent("message.voicechat.disable.enabled");
    public static final TranslatableComponent DISABLE_DISABLED = new TranslatableComponent("message.voicechat.disable.disabled");
    public static final TranslatableComponent DISABLE_NO_SPEAKER = new TranslatableComponent("message.voicechat.disable.no_speaker");

    private final Screen screen;
    private final ClientPlayerStateManager stateManager;

    public DisableTooltipSupplier(Screen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (!stateManager.canEnable()) {
            tooltip.add(DISABLE_NO_SPEAKER.getVisualOrderText());
        } else if (stateManager.isDisabled()) {
            tooltip.add(DISABLE_ENABLED.getVisualOrderText());
        } else {
            tooltip.add(DISABLE_DISABLED.getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

}
