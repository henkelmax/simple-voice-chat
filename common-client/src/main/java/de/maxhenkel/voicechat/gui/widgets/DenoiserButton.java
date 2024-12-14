package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class DenoiserButton extends BooleanConfigButton {

    private static final Component ENABLED = new TranslatableComponent("message.voicechat.denoiser.on");
    private static final Component DISABLED = new TranslatableComponent("message.voicechat.denoiser.off");

    public DenoiserButton(int x, int y, int width, int height) {
        super(x, y, width, height, VoicechatClient.CLIENT_CONFIG.denoiser, enabled -> {
            return new TranslatableComponent("message.voicechat.denoiser", enabled ? ENABLED : DISABLED);
        });
        if (Denoiser.createDenoiser() == null) {
            active = false;
        }
    }

}
