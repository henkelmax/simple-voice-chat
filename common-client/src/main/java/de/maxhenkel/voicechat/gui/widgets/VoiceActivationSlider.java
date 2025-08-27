package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.natives.RNNoiseManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER = new ResourceLocation(Voicechat.MODID, "textures/gui/voice_activation_slider.png");
    private static final ITextComponent NO_ACTIVATION = new TextComponentTranslation("message.voicechat.voice_activation.disabled").setStyle(new Style().setColor(TextFormatting.RED));

    private final SlidingMaxSmooth micValue;

    public VoiceActivationSlider(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, AudioUtils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
        micValue = new SlidingMaxSmooth();
    }

    public boolean shouldShowSlider() {
        if (!MicrophoneActivationType.VOICE.equals(VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get())) {
            return false;
        }
        if (!RNNoiseManager.canUseDenoiser()) {
            return true;
        }
        return !VoicechatClient.CLIENT_CONFIG.vad.get();
    }

    @Override
    public void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        super.mouseDragged(mc, mouseX, mouseY);
        mc.getTextureManager().bindTexture(SLIDER);
        GlStateManager.color(1F, 1F, 1F, 1F);
        int width = (int) ((getButtonWidth() - 2) * micValue.smoothMax());
        drawTexturedModalRect(x + 1, y + 1, 0, 0, width, 18);
    }

    @Override
    protected void renderSlider(Minecraft mc) {
        boolean shouldShow = shouldShowSlider();
        if (shouldShow != enabled) {
            enabled = shouldShow;
            updateMessage();
        }
        if (!enabled) {
            return;
        }
        super.renderSlider(mc);
    }

    @Override
    protected void updateMessage() {
        if (!enabled) {
            displayString = "";
            return;
        }
        long db = Math.round(AudioUtils.percToDb(value));
        TextComponentTranslation component = new TextComponentTranslation("message.voicechat.voice_activation", db);

        if (db >= -10L) {
            component.setStyle(new Style().setColor(TextFormatting.RED));
        }

        displayString = component.getFormattedText();
    }

    @Nullable
    public ITextComponent getHoverText() {
        if (!enabled) {
            return null;
        }
        if (value >= 1D) {
            return NO_ACTIVATION;
        }
        return null;
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(AudioUtils.percToDb(value)).save();
    }

    @Override
    public void onMicValue(double dB) {
        micValue.add(AudioUtils.dbToPerc(dB));
    }

    public boolean isHovered() {
        return hovered;
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
