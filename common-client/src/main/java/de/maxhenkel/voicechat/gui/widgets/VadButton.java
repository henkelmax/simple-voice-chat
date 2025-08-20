package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class VadButton extends BooleanConfigButton {

    private static final ITextComponent AUTO = new TextComponentTranslation("message.voicechat.voice_activation_detection.auto");
    private static final ITextComponent MANUAL = new TextComponentTranslation("message.voicechat.voice_activation_detection.manual");

    public VadButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, VoicechatClient.CLIENT_CONFIG.vad, e -> {
            return new TextComponentTranslation("message.voicechat.voice_activation_detection", e ? AUTO : MANUAL);
        });
        if (!Denoiser.canUseDenoiser()) {
            enabled = false;
        }
    }

}
