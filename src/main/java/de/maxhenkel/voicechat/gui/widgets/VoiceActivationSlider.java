package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceActivationSlider extends AbstractSlider {

    public VoiceActivationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, StringTextComponent.EMPTY, Utils.dbToPerc(Main.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(Utils.percToDb(value));
        setMessage(new TranslationTextComponent("message.voicechat.voice_activation", db));
    }

    @Override
    protected void applyValue() {
        Main.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(value));
        Main.CLIENT_CONFIG.voiceActivationThreshold.save();
    }

}
