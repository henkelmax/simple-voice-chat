package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.DebouncedSlider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

public class AdjustVolumeSlider extends DebouncedSlider {

    protected static final ITextComponent MUTED = new TranslationTextComponent("message.voicechat.muted");

    protected static final float MAXIMUM = 4F;

    protected final UUID player;

    public AdjustVolumeSlider(int xIn, int yIn, int widthIn, int heightIn, UUID player) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""), (player == null ? 1D : VoicechatClient.VOLUME_CONFIG.getVolume(player, 1D)) / MAXIMUM);
        this.player = player;
        if (player == null) {
            visible = false;
        }
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        if (value <= 0D) {
            setMessage(MUTED);
            return;
        }
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        setMessage(new TranslationTextComponent("message.voicechat.volume_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.VOLUME_CONFIG.setVolume(player, value * MAXIMUM);
        VoicechatClient.VOLUME_CONFIG.save();
    }
}
