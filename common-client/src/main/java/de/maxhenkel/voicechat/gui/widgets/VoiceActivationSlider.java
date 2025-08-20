package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER = new ResourceLocation(Voicechat.MODID, "textures/gui/voice_activation_slider.png");
    private static final Component NO_ACTIVATION = new TranslatableComponent("message.voicechat.voice_activation.disabled").withStyle(ChatFormatting.RED);

    private final SlidingAverage micValue;

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, TextComponent.EMPTY, AudioUtils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
        micValue = new SlidingAverage();
    }

    public boolean shouldShowSlider() {
        return Denoiser.canUseDenoiser() && !VoicechatClient.CLIENT_CONFIG.vad.get() && MicrophoneActivationType.VOICE.equals(VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get());
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int i, int j) {
        RenderSystem.setShaderTexture(0, SLIDER);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        int width = (int) ((getWidth() - 2) * micValue.average());
        blit(poseStack, x + 1, y + 1, 0, 0, width, 18);
        boolean shouldShow = shouldShowSlider();
        if (shouldShow != active) {
            active = shouldShow;
            updateMessage();
        }
        if (!active) {
            return;
        }
        super.renderBg(poseStack, minecraft, i, j);
    }

    @Override
    protected void updateMessage() {
        if (!active) {
            setMessage(new TextComponent(""));
            return;
        }
        long db = Math.round(AudioUtils.percToDb(value));
        MutableComponent component = new TranslatableComponent("message.voicechat.voice_activation", db);

        if (db >= -10L) {
            component.withStyle(ChatFormatting.RED);
        }

        setMessage(component);
    }

    @Nullable
    public Component getHoverText() {
        if (!active) {
            return null;
        }
        if (value >= 1D) {
            return NO_ACTIVATION;
        }
        return null;
    }

    public boolean isHovered() {
        return isHovered;
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(AudioUtils.percToDb(value)).save();
    }

    @Override
    public void onMicValue(double dB) {
        micValue.add(AudioUtils.dbToPerc(dB));
    }

    @Override
    public void onStop() {
        micValue.reset();
    }

    private static class SlidingAverage {
        private final double[] values = new double[5];
        private int n, p;
        private double s;

        public void add(double x) {
            if (n < values.length) {
                n++;
            } else {
                s -= values[p];
            }
            s += x;
            values[p] = x;
            p = (p + 1) % values.length;
        }

        public double average() {
            return n == 0 ? 0D : s / n;
        }

        public void reset() {
            n = 0;
            p = 0;
            s = 0D;
        }
    }

}
