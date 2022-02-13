package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceSoundSlider extends AbstractSlider {

    public VoiceSoundSlider(int x, int y, int width, int height) {
        super(x, y, width, height, new StringTextComponent(""), VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / 2F);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(getMsg());
    }

    public ITextComponent getMsg() {
        return new TranslationTextComponent("message.voicechat.voice_chat_volume", Math.round(value * 200F) + "%");
    }

    @Override
    protected void applyValue() {
        VoicechatClient.CLIENT_CONFIG.voiceChatVolume.set(value * 2F).save();
    }
}
