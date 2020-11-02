package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceActivationSlider extends AbstractSlider {

    protected VoiceActivationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, StringTextComponent.EMPTY, Utils.dbToPerc(Main.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        func_230979_b_();
    }

    @Override
    protected void func_230979_b_() {
        long db = Math.round(Utils.percToDb(sliderValue));
        setMessage(new TranslationTextComponent("message.voice_activation", db));
    }

    @Override
    protected void func_230972_a_() {
        Main.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(sliderValue));
        Main.CLIENT_CONFIG.voiceActivationThreshold.save();
    }

}
