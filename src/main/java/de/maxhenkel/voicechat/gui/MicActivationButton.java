package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Config;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.TranslationTextComponent;

public class MicActivationButton extends AbstractButton {

    private MicrophoneActivationType type;
    private VoiceActivationSlider voiceActivationSlider;

    public MicActivationButton(int xIn, int yIn, int widthIn, int heightIn, VoiceActivationSlider voiceActivationSlider) {
        super(xIn, yIn, widthIn, heightIn, null);
        this.voiceActivationSlider = voiceActivationSlider;
        type = Config.CLIENT.MICROPHONE_ACTIVATION_TYPE.get();
        updateText();
    }

    private void updateText() {
        if (type.equals(MicrophoneActivationType.PTT)) {
            setMessage(new TranslationTextComponent("message.activation_type", new TranslationTextComponent("message.activation_type.ptt")).getFormattedText());
            voiceActivationSlider.active = false;
        } else if (type.equals(MicrophoneActivationType.VOICE)) {
            setMessage(new TranslationTextComponent("message.activation_type", new TranslationTextComponent("message.activation_type.voice")).getFormattedText());
            voiceActivationSlider.active = true;
        }
    }

    @Override
    public void onPress() {
        type = MicrophoneActivationType.values()[(type.ordinal() + 1) % MicrophoneActivationType.values().length];
        Config.CLIENT.MICROPHONE_ACTIVATION_TYPE.set(type);
        Config.CLIENT.MICROPHONE_ACTIVATION_TYPE.save();
        updateText();
    }
}
