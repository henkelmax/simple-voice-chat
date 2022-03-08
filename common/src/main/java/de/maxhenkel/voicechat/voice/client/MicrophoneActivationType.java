package de.maxhenkel.voicechat.voice.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum MicrophoneActivationType {

    PTT(new TranslatableComponent("message.voicechat.activation_type.ptt")), VOICE(new TranslatableComponent("message.voicechat.activation_type.voice"));

    private final Component component;

    MicrophoneActivationType(Component component) {
        this.component = component;
    }

    public Component getText() {
        return component;
    }
}
