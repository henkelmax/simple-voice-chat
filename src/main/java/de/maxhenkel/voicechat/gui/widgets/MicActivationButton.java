package de.maxhenkel.voicechat.gui.widgets;


import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class MicActivationButton extends AbstractPressableButtonWidget {

    private MicrophoneActivationType type;
    private VoiceActivationSlider voiceActivationSlider;

    public MicActivationButton(int xIn, int yIn, int widthIn, int heightIn, VoiceActivationSlider voiceActivationSlider) {
        super(xIn, yIn, widthIn, heightIn, LiteralText.EMPTY);
        this.voiceActivationSlider = voiceActivationSlider;
        type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
        updateText();
    }

    private void updateText() {
        if (MicrophoneActivationType.PTT.equals(type)) {
            setMessage(new TranslatableText("message.voicechat.activation_type", new TranslatableText("message.voicechat.activation_type.ptt")));
            voiceActivationSlider.visible = false;
        } else if (MicrophoneActivationType.VOICE.equals(type)) {
            setMessage(new TranslatableText("message.voicechat.activation_type", new TranslatableText("message.voicechat.activation_type.voice")));
            voiceActivationSlider.visible = true;
        }
    }

    @Override
    public void onPress() {
        type = MicrophoneActivationType.values()[(type.ordinal() + 1) % MicrophoneActivationType.values().length];
        VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(type);
        VoicechatClient.CLIENT_CONFIG.microphoneActivationType.save();
        updateText();
    }
}
