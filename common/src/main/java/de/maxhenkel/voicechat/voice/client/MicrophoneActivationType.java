package de.maxhenkel.voicechat.voice.client;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum MicrophoneActivationType {

    PTT(new TranslationTextComponent("message.voicechat.activation_type.ptt")), VOICE(new TranslationTextComponent("message.voicechat.activation_type.voice"));

    private final ITextComponent component;

    MicrophoneActivationType(ITextComponent component) {
        this.component = component;
    }

    public ITextComponent getText() {
        return component;
    }
}
