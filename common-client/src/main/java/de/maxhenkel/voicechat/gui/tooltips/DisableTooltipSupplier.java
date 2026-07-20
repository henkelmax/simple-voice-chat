package de.maxhenkel.voicechat.gui.tooltips;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class DisableTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final TranslationTextComponent DISABLE_ENABLED = new TranslationTextComponent("message.voicechat.disable.enabled");
    public static final TranslationTextComponent DISABLE_DISABLED = new TranslationTextComponent("message.voicechat.disable.disabled");
    public static final TranslationTextComponent DISABLE_NO_SPEAKER = new TranslationTextComponent("message.voicechat.disable.speaker_unavailable");

    private final Screen screen;
    private final ClientPlayerStateManager stateManager;

    public DisableTooltipSupplier(Screen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void onTooltip(ImageButton button, MatrixStack matrices, int mouseX, int mouseY) {
        List<IReorderingProcessor> tooltip = new ArrayList<>();

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
