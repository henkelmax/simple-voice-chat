package de.maxhenkel.voicechat.voice.client;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public enum MicrophoneActivationType {

    PTT(new TextComponentTranslation("message.voicechat.activation_type.ptt")), VOICE(new TextComponentTranslation("message.voicechat.activation_type.voice"));

    private final ITextComponent component;

    MicrophoneActivationType(ITextComponent component) {
        this.component = component;
    }

    public ITextComponent getText() {
        return component;
    }
}
