package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class MicAmplificationSlider extends SliderWidget {

    private static final float MAXIMUM = 4F;

    protected MicAmplificationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, LiteralText.EMPTY, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue() / MAXIMUM);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        setMessage(new TranslatableText("message.voicechat.microphone_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    protected void applyValue() {
        VoicechatClient.CLIENT_CONFIG.microphoneAmplification.set(value * MAXIMUM);
        VoicechatClient.CLIENT_CONFIG.microphoneAmplification.save();
    }
}
