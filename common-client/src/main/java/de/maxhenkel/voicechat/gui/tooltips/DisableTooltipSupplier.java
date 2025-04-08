package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class DisableTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final Component DISABLE_ENABLED = Component.translatable("message.voicechat.disable.enabled");
    public static final Component DISABLE_DISABLED = Component.translatable("message.voicechat.disable.disabled");
    public static final Component DISABLE_NO_SPEAKER = Component.translatable("message.voicechat.disable.no_speaker");

    private final Screen screen;
    private final ClientPlayerStateManager stateManager;
    @Nullable
    private State lastState;

    public DisableTooltipSupplier(Screen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void updateTooltip(ImageButton button) {
        State state = getState();
        if (state != lastState) {
            lastState = state;
            button.setTooltip(Tooltip.create(state.getComponent()));
        }
    }

    private State getState() {
        if (!stateManager.canEnable()) {
            return State.NO_SPEAKER;
        } else if (stateManager.isDisabled()) {
            return State.DISABLED;
        } else {
            return State.ENABLED;
        }
    }

    private enum State {
        ENABLED(DISABLE_DISABLED),
        DISABLED(DISABLE_ENABLED),
        NO_SPEAKER(DISABLE_NO_SPEAKER);

        private final Component component;

        State(Component component) {
            this.component = component;
        }

        public Component getComponent() {
            return component;
        }
    }

}
