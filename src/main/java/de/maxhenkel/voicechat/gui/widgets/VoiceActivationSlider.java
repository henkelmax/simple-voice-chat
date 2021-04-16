package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class VoiceActivationSlider extends SliderWidget {

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, LiteralText.EMPTY, Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(Utils.percToDb(value));
        setMessage(new TranslatableText("message.voicechat.voice_activation", db));
    }

    @Override
    protected void applyValue() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(value));
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.save();
    }

}
