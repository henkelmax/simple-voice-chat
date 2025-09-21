package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.VolumeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class MicAmplificationSlider extends DebouncedSlider {

    public static final int GAIN_WARNING_THRESHOLD = 10;
    private static final Tooltip GAIN_WARNING = Tooltip.create(Component.translatable("message.voicechat.microphone_gain.warning", GAIN_WARNING_THRESHOLD).withStyle(ChatFormatting.RED));

    public MicAmplificationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, Component.empty(), gainToValue(VoicechatClient.CLIENT_CONFIG.microphoneGain.get()));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long gain = Math.round(valueToGain(value));
        MutableComponent message = Component.translatable("message.voicechat.microphone_gain", gain);
        if (gain > GAIN_WARNING_THRESHOLD && active) {
            message.withStyle(ChatFormatting.RED);
            setTooltip(GAIN_WARNING);
        } else {
            setTooltip(null);
        }
        setMessage(message);
    }

    public void setActive(boolean active) {
        this.active = active;
        updateMessage();
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
