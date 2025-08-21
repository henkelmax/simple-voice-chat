package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider");
    private static final ResourceLocation HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_highlighted");
    private static final ResourceLocation SLIDER_HANDLE_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle");
    private static final ResourceLocation SLIDER_HANDLE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle_highlighted");
    private static final ResourceLocation VOICE_ACTIVATION_SLIDER = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/gui/voice_activation_slider.png");
    private static final Component NO_ACTIVATION = Component.translatable("message.voicechat.voice_activation.disabled").withStyle(ChatFormatting.RED);

    private final SlidingMaxSmooth micValue;

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), AudioUtils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
        micValue = new SlidingMaxSmooth();
    }

    public boolean shouldShowSlider() {
        if (!MicrophoneActivationType.VOICE.equals(VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get())) {
            return false;
        }
        if (!Denoiser.canUseDenoiser()) {
            return true;
        }
        return !VoicechatClient.CLIENT_CONFIG.vad.get();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.blitSprite(RenderType::guiTextured, getSlider(), getX(), getY(), getWidth(), getHeight());

        int micWidth = (int) ((width - 2) * micValue.smoothMax());
        guiGraphics.blit(RenderType::guiTextured, VOICE_ACTIVATION_SLIDER, getX() + 1, getY() + 1, 0, 0, micWidth, 18, 256, 256);

        active = shouldShowSlider();
        if (!active) {
            return;
        }

        guiGraphics.blitSprite(RenderType::guiTextured, getHandle(), getX() + (int) (value * (double) (width - 8)), getY(), 8, 20);
        renderScrollingString(guiGraphics, minecraft.font, 2, 16777215);
    }

    private ResourceLocation getSlider() {
        return isFocused() && !(isHovered || isFocused()) ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
    }

    private ResourceLocation getHandle() {
        return !isHovered && !isFocused() ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(AudioUtils.percToDb(value));
        MutableComponent component = Component.translatable("message.voicechat.voice_activation", db);

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

    private static class SlidingMaxSmooth {
        private final double[] values = new double[15];
        private int n, p;

        private static final double SMOOTHING_PER_SEC = 25D;

        private double smoothed;
        private long lastNs = -1L;

        public void add(double x) {
            if (n < values.length) {
                n++;
            }
            values[p] = x;
            p = (p + 1) % values.length;
        }

        public double max() {
            if (n == 0) {
                return 0D;
            }
            int len = Math.min(n, values.length);
            double max = values[0];
            for (int i = 1; i < len; i++) {
                if (values[i] > max) {
                    max = values[i];
                }
            }
            return max;
        }

        public double smoothMax() {
            long nowNanos = System.nanoTime();
            double target = max();

            if (lastNs < 0) {
                lastNs = nowNanos;
                smoothed = target;
                return smoothed;
            }

            double dt = (nowNanos - lastNs) / 1_000_000_000D;
            lastNs = nowNanos;

            double alpha = dt * SMOOTHING_PER_SEC;
            if (alpha > 1D) {
                alpha = 1D;
            }
            if (alpha < 0D) {
                alpha = 0D;
            }

            smoothed += (target - smoothed) * alpha;
            return smoothed;
        }

        public void reset() {
            n = 0;
            p = 0;
            smoothed = 0D;
            lastNs = -1L;
        }
    }

}
