package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class VoiceSoundSlider extends SliderWidget {

    public VoiceSoundSlider(int x, int y, int width, int theight) {
        super(x, y, width, theight, LiteralText.EMPTY, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / 2F);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(getMsg());
    }

    public Text getMsg() {
        return new TranslatableText("message.voicechat.voice_chat_volume", Math.round(value * 200F) + "%");
    }

    @Override
    protected void applyValue() {
        VoicechatClient.CLIENT_CONFIG.voiceChatVolume.set(value * 2F);
        VoicechatClient.CLIENT_CONFIG.voiceChatVolume.save();
    }
}
