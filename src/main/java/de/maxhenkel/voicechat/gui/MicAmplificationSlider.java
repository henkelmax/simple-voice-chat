package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Main;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MicAmplificationSlider extends AbstractSlider {

    private static final float MAXIMUM = 4F;

    protected MicAmplificationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""), Main.CLIENT_CONFIG.microphoneAmplification.get().floatValue() / MAXIMUM);
        func_230979_b_();
    }

    @Override
    protected void func_230979_b_() {
        long amp = Math.round(field_230683_b_ * MAXIMUM * 100F - 100F);
        func_238482_a_(new TranslationTextComponent("message.microphone_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    protected void func_230972_a_() {
        Main.CLIENT_CONFIG.microphoneAmplification.set(field_230683_b_ * MAXIMUM);
        Main.CLIENT_CONFIG.microphoneAmplification.save();
    }
}
