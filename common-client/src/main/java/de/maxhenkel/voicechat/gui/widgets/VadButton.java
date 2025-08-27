package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.natives.RNNoiseManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class VadButton extends BooleanConfigButton {

    private static final Component AUTO = new TranslatableComponent("message.voicechat.voice_activation_detection.auto");
    private static final Component MANUAL = new TranslatableComponent("message.voicechat.voice_activation_detection.manual");

    public VadButton(int x, int y, int width, int height) {
        super(x, y, width, height, VoicechatClient.CLIENT_CONFIG.vad, enabled -> {
            return new TranslatableComponent("message.voicechat.voice_activation_detection", enabled ? AUTO : MANUAL);
        });
        if (!RNNoiseManager.canUseDenoiser()) {
            active = false;
            setMessage(component.apply(false));
        }
    }

}
