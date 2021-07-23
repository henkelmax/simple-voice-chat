package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class VoiceActivationSlider extends AbstractSliderButton {

    public VoiceActivationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY, Utils.dbToPerc(Main.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(Utils.percToDb(value));
        setMessage(new TranslatableComponent("message.voicechat.voice_activation", db));
    }

    @Override
    protected void applyValue() {
        Main.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(value));
        Main.CLIENT_CONFIG.voiceActivationThreshold.save();
    }

}
