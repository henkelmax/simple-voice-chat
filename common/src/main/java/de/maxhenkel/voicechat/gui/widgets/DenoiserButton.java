package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import net.minecraft.network.chat.Component;

public class DenoiserButton extends BooleanConfigButton {

    private static final Component ENABLED = Component.translatable("message.voicechat.enabled");
    private static final Component DISABLED = Component.translatable("message.voicechat.disabled");

    public DenoiserButton(int x, int y, int width, int height) {
        super(x, y, width, height, VoicechatClient.CLIENT_CONFIG.denoiser, enabled -> {
            return Component.translatable("message.voicechat.denoiser", enabled ? ENABLED : DISABLED);
        });
        if (Denoiser.createDenoiser() == null) {
            active = false;
        }
    }

}
