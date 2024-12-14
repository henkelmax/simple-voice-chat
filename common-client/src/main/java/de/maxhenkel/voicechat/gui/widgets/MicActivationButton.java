package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class MicActivationButton extends EnumButton<MicrophoneActivationType> {

    protected Consumer<MicrophoneActivationType> onChange;

    public MicActivationButton(int xIn, int yIn, int widthIn, int heightIn, Consumer<MicrophoneActivationType> onChange) {
        super(xIn, yIn, widthIn, heightIn, VoicechatClient.CLIENT_CONFIG.microphoneActivationType);
        this.onChange = onChange;
        updateText();
        onChange.accept(entry.get());
    }

    @Override
    protected Component getText(MicrophoneActivationType type) {
        return Component.translatable("message.voicechat.activation_type", type.getText());
    }

    @Override
    protected void onUpdate(MicrophoneActivationType type) {
        onChange.accept(type);
    }

}
