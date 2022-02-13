package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AdjustVolumeSlider extends AbstractSlider {

    private static final float MAXIMUM = 4F;

    private PlayerState player;

    public AdjustVolumeSlider(int xIn, int yIn, int widthIn, int heightIn, PlayerState player) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""), (player == null ? 1D : VoicechatClient.VOLUME_CONFIG.getVolume(player.getUuid(), 1D)) / MAXIMUM);
        this.player = player;
        if (player == null) {
            visible = false;
        }
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        setMessage(new TranslationTextComponent("message.voicechat.volume_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    protected void applyValue() {
        VoicechatClient.VOLUME_CONFIG.setVolume(player.getUuid(), value * MAXIMUM);
        VoicechatClient.VOLUME_CONFIG.save();
    }
}
