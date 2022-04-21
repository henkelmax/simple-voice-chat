package de.maxhenkel.voicechat.voice.client.speaker;

import net.minecraft.network.chat.Component;

public enum AudioType {

    NORMAL(Component.translatable("message.voicechat.audio_type.normal")), REDUCED(Component.translatable("message.voicechat.audio_type.reduced")), OFF(Component.translatable("message.voicechat.audio_type.off"));

    private final Component component;

    AudioType(Component component) {
        this.component = component;
    }

    public Component getText() {
        return component;
    }
}
