package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class VoiceActivationSlider extends DebouncedSlider {

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, TextComponent.EMPTY, Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(Utils.percToDb(value));
        setMessage(new TranslatableComponent("message.voicechat.voice_activation", db));
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(value)).save();
    }

}
