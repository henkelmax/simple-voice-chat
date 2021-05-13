package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TranslatableComponent;

public class VoiceActivationSlider extends AbstractSliderButton {

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(Utils.percToDb(value));
        setMessage(new TranslatableComponent("message.voicechat.voice_activation", db).getColoredString());
    }

    @Override
    protected void applyValue() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(value));
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.save();
    }

}
