package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.VolumeManager;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MicAmplificationSlider extends DebouncedSlider {

    public MicAmplificationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""), gainToValue(VoicechatClient.CLIENT_CONFIG.microphoneGain.get()));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long gain = Math.round(valueToGain(value));
        setMessage(new TranslationTextComponent("message.voicechat.microphone_gain", gain));
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.microphoneGain.set(valueToGain(value)).save();
    }

    private static double gainToValue(double gain) {
        return (gain - VolumeManager.MIN_GAIN) / (VolumeManager.MAX_GAIN - VolumeManager.MIN_GAIN);
    }

    private static double valueToGain(double value) {
        return value * (VolumeManager.MAX_GAIN - VolumeManager.MIN_GAIN) + VolumeManager.MIN_GAIN;
    }

}
