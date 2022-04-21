package de.maxhenkel.voicechat.voice.client;

import net.minecraft.network.chat.Component;

public enum MicrophoneActivationType {

    PTT(Component.translatable("message.voicechat.activation_type.ptt")), VOICE(Component.translatable("message.voicechat.activation_type.voice"));

    private final Component component;

    MicrophoneActivationType(Component component) {
        this.component = component;
    }

    public Component getText() {
        return component;
    }
}
