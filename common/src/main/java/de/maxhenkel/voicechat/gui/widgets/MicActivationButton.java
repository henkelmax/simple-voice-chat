package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Consumer;

public class MicActivationButton extends EnumButton<MicrophoneActivationType> {

    protected Consumer<MicrophoneActivationType> onChange;

    public MicActivationButton(int id, int xIn, int yIn, int widthIn, int heightIn, Consumer<MicrophoneActivationType> onChange) {
        super(id, xIn, yIn, widthIn, heightIn, VoicechatClient.CLIENT_CONFIG.microphoneActivationType);
        this.onChange = onChange;
        updateText();
        onChange.accept(entry.get());
    }

    @Override
    protected ITextComponent getText(MicrophoneActivationType type) {
        return new TextComponentTranslation("message.voicechat.activation_type", type.getText());
    }

    @Override
    protected void onUpdate(MicrophoneActivationType type) {
        onChange.accept(type);
    }

}
