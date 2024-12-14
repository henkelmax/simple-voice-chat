package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class MicAmplificationSlider extends DebouncedSlider {

    private static final float MAXIMUM = 4F;

    public MicAmplificationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue() / MAXIMUM);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        setMessage(new TranslatableComponent("message.voicechat.microphone_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.microphoneAmplification.set(value * MAXIMUM).save();
    }
}
