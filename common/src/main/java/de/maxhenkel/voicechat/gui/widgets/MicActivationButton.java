package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MicActivationButton extends AbstractButton {

    private static final ITextComponent PTT = new TranslationTextComponent("message.voicechat.activation_type.ptt");
    private static final ITextComponent VOICE = new TranslationTextComponent("message.voicechat.activation_type.voice");

    private MicrophoneActivationType type;
    private VoiceActivationSlider voiceActivationSlider;

    public MicActivationButton(int xIn, int yIn, int widthIn, int heightIn, VoiceActivationSlider voiceActivationSlider) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""));
        this.voiceActivationSlider = voiceActivationSlider;
        type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
        updateText();
    }

    private void updateText() {
        if (MicrophoneActivationType.PTT.equals(type)) {
            setMessage(new TranslationTextComponent("message.voicechat.activation_type", PTT));
            voiceActivationSlider.visible = false;
        } else if (MicrophoneActivationType.VOICE.equals(type)) {
            setMessage(new TranslationTextComponent("message.voicechat.activation_type", VOICE));
            voiceActivationSlider.visible = true;
        }
    }

    @Override
    public void onPress() {
        type = MicrophoneActivationType.values()[(type.ordinal() + 1) % MicrophoneActivationType.values().length];
        VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(type).save();
        updateText();
    }
}
