package de.maxhenkel.voicechat.gui.tooltips;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class MuteTooltipSupplier implements ImageButton.TooltipSupplier {

    public static final Component MUTE_UNMUTED = Component.translatable("message.voicechat.mute.disabled");
    public static final Component MUTE_MUTED = Component.translatable("message.voicechat.mute.enabled");
    public static final Component MUTE_DISABLED_PTT = Component.translatable("message.voicechat.mute.disabled_ptt");

    private Screen screen;
    private ClientPlayerStateManager stateManager;
    @Nullable
    private State lastState;

    public MuteTooltipSupplier(Screen screen, ClientPlayerStateManager stateManager) {
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

    public static boolean canMuteMic() {
        return VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
    }

    private State getState() {
        if (!canMuteMic()) {
            return State.DISABLED_PTT;
        } else if (stateManager.isMuted()) {
            return State.MUTED;
        } else {
            return State.UNMUTED;
        }
    }

    private enum State {
        UNMUTED(MUTE_UNMUTED),
        MUTED(MUTE_MUTED),
        DISABLED_PTT(MUTE_DISABLED_PTT);

        private final Component component;

        State(Component component) {
            this.component = component;
        }

        public Component getComponent() {
            return component;
        }
    }

}
