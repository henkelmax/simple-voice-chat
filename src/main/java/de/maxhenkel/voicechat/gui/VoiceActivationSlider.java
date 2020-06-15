package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Config;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceActivationSlider extends AbstractSlider {

    protected VoiceActivationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, Utils.dbToPerc(Config.CLIENT.VOICE_ACTIVATION_THRESHOLD.get().floatValue()));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(Utils.percToDb(value));
        setMessage(new TranslationTextComponent("message.voice_activation", db).getFormattedText());
    }

    @Override
    protected void applyValue() {
        Config.CLIENT.VOICE_ACTIVATION_THRESHOLD.set(Utils.percToDb(value));
        Config.CLIENT.VOICE_ACTIVATION_THRESHOLD.save();
    }

}
