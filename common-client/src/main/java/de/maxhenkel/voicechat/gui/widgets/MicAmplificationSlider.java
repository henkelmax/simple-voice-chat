package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.VolumeManager;
import net.minecraft.util.text.TextComponentTranslation;

public class MicAmplificationSlider extends DebouncedSlider {

    public MicAmplificationSlider(int id, int xIn, int yIn, int widthIn, int heightIn) {
        super(id, xIn, yIn, widthIn, heightIn, gainToValue(VoicechatClient.CLIENT_CONFIG.microphoneGain.get()));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long gain = Math.round(valueToGain(value));
        displayString = new TextComponentTranslation("message.voicechat.microphone_gain", gain).getUnformattedComponentText();
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
