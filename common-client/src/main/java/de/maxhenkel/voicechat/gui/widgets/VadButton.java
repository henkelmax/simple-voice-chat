package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VadButton extends BooleanConfigButton {

    private static final ITextComponent AUTO = new TranslationTextComponent("message.voicechat.voice_activation_detection.auto");
    private static final ITextComponent MANUAL = new TranslationTextComponent("message.voicechat.voice_activation_detection.manual");

    public VadButton(int x, int y, int width, int height) {
        super(x, y, width, height, VoicechatClient.CLIENT_CONFIG.vad, enabled -> {
            return new TranslationTextComponent("message.voicechat.voice_activation_detection", enabled ? AUTO : MANUAL);
        });
        if (!Denoiser.canUseDenoiser()) {
            active = false;
        }
    }

}
