package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class VoiceSoundSlider extends AbstractSliderButton {

    public VoiceSoundSlider(int x, int y, int width, int height) {
        super(x, y, width, height, TextComponent.EMPTY, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / 2F);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(getMsg());
    }

    public Component getMsg() {
        return new TranslatableComponent("message.voicechat.voice_chat_volume", Math.round(value * 200F) + "%");
    }

    @Override
    protected void applyValue() {
        VoicechatClient.CLIENT_CONFIG.voiceChatVolume.set(value * 2F).save();
    }
}
