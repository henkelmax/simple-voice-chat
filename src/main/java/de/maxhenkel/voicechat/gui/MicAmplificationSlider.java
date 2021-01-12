package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Main;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.TranslationTextComponent;

public class MicAmplificationSlider extends AbstractSlider {

    private static final float MAXIMUM = 4F;

    protected MicAmplificationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, Main.CLIENT_CONFIG.microphoneAmplification.get().floatValue() / MAXIMUM);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        setMessage(new TranslationTextComponent("message.microphone_amplification", (amp > 0F ? "+" : "") + amp + "%").getString());
    }

    @Override
    protected void applyValue() {
        Main.CLIENT_CONFIG.microphoneAmplification.set(value * MAXIMUM);
        Main.CLIENT_CONFIG.microphoneAmplification.save();
    }
}
