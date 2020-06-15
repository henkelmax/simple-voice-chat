package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Config;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class AdjustVolumeSlider extends AbstractSlider {

    private static final float MAXIMUM = 4F;

    private PlayerEntity player;

    protected AdjustVolumeSlider(int xIn, int yIn, int widthIn, int heightIn, PlayerEntity player) {
        super(xIn, yIn, widthIn, heightIn, (player == null ? 1D : Config.VOLUME_CONFIG.getVolume(player.getUniqueID(), 1D)) / MAXIMUM);
        this.player = player;
        if (player == null) {
            active = false;
        }
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        setMessage(new TranslationTextComponent("message.volume_amplification", (amp > 0F ? "+" : "") + amp + "%").getFormattedText());
    }

    @Override
    protected void applyValue() {
        Config.VOLUME_CONFIG.setVolume(player.getUniqueID(), value * MAXIMUM);
    }
}
