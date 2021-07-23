package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Main;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class VoiceSoundSlider extends AbstractSliderButton {

    public VoiceSoundSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY, Main.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / 2F);
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
        Main.CLIENT_CONFIG.voiceChatVolume.set(value * 2F);
        Main.CLIENT_CONFIG.voiceChatVolume.save();
    }
}
