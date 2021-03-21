package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class AdjustVolumeSlider extends SliderWidget {

    private static final float MAXIMUM = 4F;

    private PlayerState player;

    public AdjustVolumeSlider(int xIn, int yIn, int widthIn, int heightIn, PlayerState player) {
        super(xIn, yIn, widthIn, heightIn, LiteralText.EMPTY, (player == null ? 1D : VoicechatClient.VOLUME_CONFIG.getVolume(player.getGameProfile().getId(), 1D)) / MAXIMUM);
        this.player = player;
        if (player == null) {
            visible = false;
        }
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        setMessage(new TranslatableText("message.voicechat.volume_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    protected void applyValue() {
        VoicechatClient.VOLUME_CONFIG.setVolume(player.getGameProfile().getId(), value * MAXIMUM);
        VoicechatClient.VOLUME_CONFIG.save();
    }
}
