package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class DenoiserButton extends BooleanConfigButton {

    private static final ITextComponent ENABLED = new TranslationTextComponent("message.voicechat.denoiser.on");
    private static final ITextComponent DISABLED = new TranslationTextComponent("message.voicechat.denoiser.off");

    public DenoiserButton(int x, int y, int width, int height) {
        super(x, y, width, height, VoicechatClient.CLIENT_CONFIG.denoiser, enabled -> {
            return new TranslationTextComponent("message.voicechat.denoiser", enabled ? ENABLED : DISABLED);
        });
        if (Denoiser.createDenoiser() == null) {
            active = false;
        }
    }

}
