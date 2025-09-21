package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.VolumeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class MicAmplificationSlider extends DebouncedSlider {

    public static final int GAIN_WARNING_THRESHOLD = 10;
    private static final ITextComponent GAIN_WARNING = new TranslationTextComponent("message.voicechat.microphone_gain.warning", GAIN_WARNING_THRESHOLD).withStyle(TextFormatting.RED);

    private final Screen parent;

    public MicAmplificationSlider(Screen parent, int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""), gainToValue(VoicechatClient.CLIENT_CONFIG.microphoneGain.get()));
        this.parent = parent;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long gain = Math.round(valueToGain(value));
        TranslationTextComponent message = new TranslationTextComponent("message.voicechat.microphone_gain", gain);
        if (gain > GAIN_WARNING_THRESHOLD && active) {
            message.withStyle(TextFormatting.RED);
        }
        setMessage(message);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int i, int j, float f) {
        super.renderButton(matrixStack, i, j, f);
        if (isHovered()) {
            renderToolTip(matrixStack, i, j);
        }
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, int i, int j) {
        super.renderToolTip(matrixStack, i, j);
        long gain = Math.round(valueToGain(value));
        if (gain > GAIN_WARNING_THRESHOLD && active) {
            parent.renderTooltip(matrixStack, Minecraft.getInstance().font.split(GAIN_WARNING, 200), i, j);
        }
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
