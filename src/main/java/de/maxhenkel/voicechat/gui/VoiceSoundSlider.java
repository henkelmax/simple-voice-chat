package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Main;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceSoundSlider extends AbstractSlider {

    protected VoiceSoundSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, StringTextComponent.EMPTY, Main.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / 2F);
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
        Main.CLIENT_CONFIG.voiceChatVolume.set(value * 2F);
        Main.CLIENT_CONFIG.voiceChatVolume.save();
    }
}
