package de.maxhenkel.voicechat.voice.client.speaker;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public enum AudioType {

    NORMAL(new TextComponentTranslation("message.voicechat.audio_type.normal")), REDUCED(new TextComponentTranslation("message.voicechat.audio_type.reduced")), OFF(new TextComponentTranslation("message.voicechat.audio_type.off"));

    private final ITextComponent component;

    AudioType(ITextComponent component) {
        this.component = component;
    }

    public ITextComponent getText() {
        return component;
    }
}
