package de.maxhenkel.voicechat.voice.client.speaker;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum AudioType {

    NORMAL(new TranslatableComponent("message.voicechat.audio_type.normal")), REDUCED(new TranslatableComponent("message.voicechat.audio_type.reduced")), OFF(new TranslatableComponent("message.voicechat.audio_type.off"));

    private final Component component;

    AudioType(Component component) {
        this.component = component;
    }

    public Component getText() {
        return component;
    }
}
