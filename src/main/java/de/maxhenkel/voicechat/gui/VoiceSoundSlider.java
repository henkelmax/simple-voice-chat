package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Config;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceSoundSlider extends AbstractSlider {

    protected VoiceSoundSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, Config.CLIENT.VOICE_CHAT_VOLUME.get().floatValue() / 2F);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(new TranslationTextComponent("message.voice_chat_volume", Math.round(value * 200F) + "%").getFormattedText());
    }

    @Override
    protected void applyValue() {
        Config.CLIENT.VOICE_CHAT_VOLUME.set(value * 2F);
        Config.CLIENT.VOICE_CHAT_VOLUME.save();
    }
}
