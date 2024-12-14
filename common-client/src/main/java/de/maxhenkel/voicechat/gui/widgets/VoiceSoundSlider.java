package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.util.text.TextComponentTranslation;

public class VoiceSoundSlider extends DebouncedSlider {

    public VoiceSoundSlider(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / 2F);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        displayString = getMsg();
    }

    public String getMsg() {
        return new TextComponentTranslation("message.voicechat.voice_chat_volume", Math.round(value * 200F) + "%").getUnformattedComponentText();
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceChatVolume.set(value * 2F).save();
    }
}
