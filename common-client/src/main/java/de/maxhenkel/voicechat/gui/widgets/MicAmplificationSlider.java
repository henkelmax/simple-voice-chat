package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.VolumeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.MutableComponent;

public class MicAmplificationSlider extends DebouncedSlider {

    public static final int GAIN_WARNING_THRESHOLD = 10;
    private static final Component GAIN_WARNING = new TranslatableComponent("message.voicechat.microphone_gain.warning", GAIN_WARNING_THRESHOLD).withStyle(ChatFormatting.RED);

    private final Screen parent;

    public MicAmplificationSlider(Screen parent, int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY, gainToValue(VoicechatClient.CLIENT_CONFIG.microphoneGain.get()));
        this.parent = parent;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long gain = Math.round(valueToGain(value));
        MutableComponent message = new TranslatableComponent("message.voicechat.microphone_gain", gain);
        if (gain > GAIN_WARNING_THRESHOLD && active) {
            message.withStyle(ChatFormatting.RED);
        }
        setMessage(message);
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        super.renderButton(poseStack, i, j, f);
        if (isHoveredOrFocused()) {
            renderToolTip(poseStack, i, j);
        }
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int i, int j) {
        super.renderToolTip(poseStack, i, j);
        long gain = Math.round(valueToGain(value));
        if (gain > GAIN_WARNING_THRESHOLD && active) {
            parent.renderTooltip(poseStack, Minecraft.getInstance().font.split(GAIN_WARNING, 200), i, j);
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
