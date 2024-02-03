package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class DenoiserButton extends BooleanConfigButton {

    private static final ITextComponent ENABLED = new TextComponentTranslation("message.voicechat.enabled");
    private static final ITextComponent DISABLED = new TextComponentTranslation("message.voicechat.disabled");

    public DenoiserButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, VoicechatClient.CLIENT_CONFIG.denoiser, enabled -> {
            return new TextComponentTranslation("message.voicechat.denoiser", enabled ? ENABLED : DISABLED);
        });
        if (Denoiser.createDenoiser() == null) {
            enabled = false;
        }
    }

}
